package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

public class GraphBuilderImplTest
{
	private GraphBuilderImpl gb;

	@Before
	public void setup()
	{
		gb = new GraphBuilderImpl(new EventFabricImpl());
	}

	@Test
	public void singleton_providesTheObject() throws Exception
	{
		final Object object = new Object();
		final Dep<Object> sut = gb.singleton(object);

		assertThat(sut.get(), is(object));
	}

	@Test
	public void functionalProvider_providesObject() throws Exception
	{
		final Dep<String> dep = gb.singleton("Hello");

		final Dep<String> sut = gb.oblivious(x -> x + "!!!", dep);

		assertThat(sut.get(), is("Hello!!!"));
	}

	@Test
	public void functionalProvider_isCaching() throws Exception
	{
		final Dep<String> dep = gb.singleton("Hello");
		final AtomicInteger count = new AtomicInteger(0);

		final Dep<String> sut = gb.oblivious(
				x -> x + count.incrementAndGet(), dep);

		assertThat(sut.get(), is("Hello1"));
		assertThat(sut.get(), is("Hello1"));
	}

	@Test
	public void functionalProvider_isEager() throws Exception
	{
		final Dep<String> dep = gb.singleton("Hello");
		final AtomicInteger count = new AtomicInteger(0);

		final Dep<String> sut = gb.oblivious(x -> x + count.incrementAndGet(), dep);

		assertThat(count.get(), is(1));
		assertThat(sut.get(), is("Hello1"));
		assertThat(count.get(), is(1));
	}

	@Test
	public void mutableProvider_providesInitialValue() throws Exception
	{
		assertThat(gb.mutable("Initial").get(), is("Initial"));
	}

	@Test
	public void mutableProvider_canChangeValue() throws Exception
	{
		final MutableProvider<String> sut = gb.mutable("Initial");
		sut.set("New value");
		assertThat(sut.get(), is("New value"));
	}

	@Test
	public void function_ofMutable_updatesToNewValue() throws Exception
	{
		final MutableProvider<String> mutable = gb.mutable("Init");

		final Dep<String> sut = gb.oblivious(x -> x + "!!!", mutable);

		assertThat(sut.get(), is("Init!!!"));
		mutable.set("New");
		assertThat(sut.get(), is("New!!!"));
	}

	@Test
	public void function_chain_update() throws Exception
	{
		final MutableProvider<String> val = gb.mutable("Init");
		final Dep<String> sut = gb.oblivious(x -> x + "!", val);
		final Dep<String> ffx = gb.oblivious(x -> x + "??", sut);

		assertThat(ffx.get(), is("Init!??"));
		val.set("Wow");
		assertThat(ffx.get(), is("Wow!??"));
	}

	@Test
	public void bifunctionalProvider_providesObject() throws Exception
	{
		final Dep<String> dep1 = gb.singleton("Hello");

		final Dep<String> dep2 = gb.singleton("dependencies");

		final Dep<String> sut = gb.oblivious2((x, y) -> x + " " + y, dep1, dep2);

		assertThat(sut.get(), is("Hello dependencies"));
	}

	@Test
	public void bifunctionalProvider_isCaching() throws Exception
	{
		final Dep<String> dep1 = gb.singleton("Hello");

		final Dep<String> dep2 = gb.singleton("dependencies");
		final AtomicInteger count = new AtomicInteger(0);

		final Dep<String> sut = gb.oblivious2(
				(x, y) -> x + count.incrementAndGet() + y, dep1, dep2);

		assertThat(sut.get(), is("Hello1dependencies"));
		assertThat(sut.get(), is("Hello1dependencies"));
	}

	@Test
	public void bifunctionalProvider_isEager() throws Exception
	{
		final Dep<String> dep1 = gb.singleton("whatever");
		final Dep<String> dep2 = gb.singleton("stuff");
		final AtomicInteger count = new AtomicInteger(0);

		final Dep<String> sut = gb.oblivious2((x, y) -> x + count.incrementAndGet() + y, dep1, dep2);

		assertThat(count.get(), is(1));
		assertThat(sut.get(), is("whatever1stuff"));
		assertThat(count.get(), is(1));
	}

	@Test
	public void bifunction_ofMutableFirst_updatesToNewValue() throws Exception
	{
		final MutableProvider<String> dep1 = gb.mutable("Init");
		final Dep<String> dep2 = gb.singleton("value");

		final Dep<String> sut = gb.oblivious2((x, y) -> x + " " + y, dep1, dep2);

		assertThat(sut.get(), is("Init value"));
		dep1.set("New");
		assertThat(sut.get(), is("New value"));
	}

	@Test
	public void bifunction_ofMutableSecond_updatesToNewValue() throws Exception
	{
		final Dep<String> dep1 = gb.singleton("Some");
		final MutableProvider<String> dep2 = gb.mutable("value");

		final Dep<String> sut = gb.oblivious2((x, y) -> x + " " + y, dep1, dep2);

		assertThat(sut.get(), is("Some value"));
		dep2.set("stuff");
		assertThat(sut.get(), is("Some stuff"));
	}

	@Test
	public void bifunction_chain_update() throws Exception
	{
		final MutableProvider<String> vx = gb.mutable("x");
		final MutableProvider<String> vy = gb.mutable("y");
		final Dep<String> sut = gb.oblivious2((x, y) -> x + "," + y, vx, vy);
		final Dep<String> bracket = gb.oblivious(x -> "[" + x + "]", sut);

		assertThat(bracket.get(), is("[x,y]"));
		vx.set("xx");
		assertThat(bracket.get(), is("[xx,y]"));
		vy.set("yy");
		assertThat(bracket.get(), is("[xx,yy]"));
	}

	@Test
	public void bifunction_bothArgsSame_calledOnceOnUpdate() throws Exception
	{
		final MutableProvider<String> vx = gb.mutable("x");
		final AtomicInteger count = new AtomicInteger(0);
		final Dep<String> sut = gb.oblivious2((x, y) -> {
			count.incrementAndGet();
			return x + y;
		}, vx, vx);

		assertThat(count.get(), is(1));
		vx.set("new x");
		assertThat(count.get(), is(2));
		assertThat(sut.get(), is("new xnew x"));
	}

	@Test
	public void bifunction_triangle_calledOnceOnUpdate() throws Exception
	{
		final MutableProvider<String> vx = gb.mutable("x");
		final Dep<String> bracket = gb.oblivious(x -> "[" + x + "]", vx);
		final AtomicInteger count = new AtomicInteger(0);
		final Dep<String> sut = gb.oblivious2((x, y) -> {
			count.incrementAndGet();
			return x + y;
		}, vx, bracket);

		assertThat(count.get(), is(1));
		vx.set("new x");
		assertThat(count.get(), is(2));
		assertThat(sut.get(), is("new x[new x]"));
	}

	@Test
	public void bifunction_diamond_calledOnceOnUpdate() throws Exception
	{
		final MutableProvider<String> vx = gb.mutable("x");
		final Dep<String> bracket = gb.oblivious(x -> "[" + x + "]", vx);
		final Dep<String> curly = gb.oblivious(x -> "{" + x + "}", vx);
		final AtomicInteger count = new AtomicInteger(0);
		final Dep<String> sut = gb.oblivious2((x, y) -> {
			count.incrementAndGet();
			return x + y;
		}, curly, bracket);

		assertThat(count.get(), is(1));
		vx.set("new x");
		assertThat(count.get(), is(2));
		assertThat(sut.get(), is("{new x}[new x]"));
	}

	@Test
	public void absorber_sameValue() throws Exception
	{
		final MutableProvider<String> dep = gb.mutable("x");
		final Dep<String> sut = gb.absorb(dep);

		assertThat(sut.get(), is("x"));
	}

	@Test
	public void absorber_onChange_newValue() throws Exception
	{
		final MutableProvider<String> dep = gb.mutable("x");
		final Dep<String> sut = gb.absorb(dep);

		assertThat(sut.get(), is("x"));

		dep.set("y");

		assertThat(sut.get(), is("y"));
	}

	@Test
	public void absorber_onChange_downstreamFunctionIsCalled() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> dep = gb.mutable("x");
		final Dep<String> sut = gb.absorb(dep);
		final Dep<String> fn = gb.oblivious(x -> {
			count.incrementAndGet();
			return "f[" + x + "]";
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[x]"));
		assertThat(count.get(), is(1));

		dep.set("y");

		assertThat(count.get(), is(2));
		assertThat(fn.get(), is("f[y]"));
		assertThat(count.get(), is(2));
	}

	@Test
	public void absorber_onNoChange_downstreamFunctionIsNotCalled() throws Exception
	{
		final String value = "x";
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> dep = gb.mutable(value);
		final Dep<String> sut = gb.absorb(dep);
		final Dep<String> fn = gb.oblivious(x -> {
			count.incrementAndGet();
			return "f[" + x + "]";
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[x]"));
		assertThat(count.get(), is(1));

		dep.set(value);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[x]"));
		assertThat(count.get(), is(1));
	}

	@Test
	public void absorber_onEqualButNotIdentical_downstreamFunctionIsCalled() throws Exception
	{
		final Integer val0 = new Integer(0);
		final Integer val1 = new Integer(0);

		assertTrue("Should be different objects but equal", val0 != val1 && val0.equals(val1));

		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<Integer> dep = gb.mutable(val0);
		final Dep<Integer> sut = gb.absorb(dep);
		final Dep<String> fn = gb.oblivious(x -> {
			count.incrementAndGet();
			return x.toString();
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("0"));
		assertThat(count.get(), is(1));

		dep.set(val1);

		assertThat(count.get(), is(2));
		assertThat(fn.get(), is("0"));
		assertThat(count.get(), is(2));
	}

	@Test
	public void absorbEqual_onChange_downstreamFunctionIsCalled() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> dep = gb.mutable("x");
		final Dep<String> sut = gb.absorbEqual(dep);
		final Dep<String> fn = gb.oblivious(x -> {
			count.incrementAndGet();
			return "f[" + x + "]";
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[x]"));
		assertThat(count.get(), is(1));

		dep.set("y");

		assertThat(count.get(), is(2));
		assertThat(fn.get(), is("f[y]"));
		assertThat(count.get(), is(2));
	}

	@Test
	public void absorbEqual_onNoChange_downstreamFunctionIsNotCalled() throws Exception
	{
		final String value = "x";
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> dep = gb.mutable(value);
		final Dep<String> sut = gb.absorbEqual(dep);
		final Dep<String> fn = gb.oblivious(x -> {
			count.incrementAndGet();
			return "f[" + x + "]";
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[x]"));
		assertThat(count.get(), is(1));

		dep.set(value);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[x]"));
		assertThat(count.get(), is(1));
	}

	@Test
	public void absorbEqual_onEqualButNotIdentical_downstreamFunctionIsNotCalled() throws Exception
	{
		final Integer val0 = new Integer(0);
		final Integer val1 = new Integer(0);

		assertTrue("Should be different objects but equal", val0 != val1 && val0.equals(val1));

		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<Integer> dep = gb.mutable(val0);
		final Dep<Integer> sut = gb.absorbEqual(dep);
		final Dep<String> fn = gb.oblivious(x -> {
			count.incrementAndGet();
			return x.toString();
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("0"));
		assertThat(count.get(), is(1));

		dep.set(val1);

		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("0"));
		assertThat(count.get(), is(1));
	}

	private static class Recomputer
	{
		private final Supplier<String> sup;

		private Recomputer(final Supplier<String> sup)
		{
			this.sup = checkNotNull(sup);
		}

		public String compute()
		{
			return "++" + sup.get() + "++";
		}
	}

	@Test
	public void supplier_throughFunction_supplies() throws Exception
	{
		final MutableProvider<String> dep = gb.mutable("a");
		final Dep<Supplier<String>> sut = gb.supplier(dep);
		final Dep<Recomputer> fn = gb.oblivious(Recomputer::new, sut);
		assertThat(fn.get().compute(), is("++a++"));
		dep.set("b");
		assertThat(fn.get().compute(), is("++b++"));
	}

	@Test
	public void supplier_changeEvent_notPropagated_throughFunction() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> dep = gb.mutable("a");
		final Dep<Recomputer> sut = gb.oblivious(s -> {
			count.incrementAndGet();
			return new Recomputer(s);
		}, gb.supplier(dep));
		assertThat(count.get(), is(1));
		dep.set("b");
		assertThat(count.get(), is(1));
		assertThat(sut.get().compute(), is("++b++"));
		assertThat(count.get(), is(1));
	}

	private static class Recomputer2
	{
		private final Supplier<String> sup1;
		private final Supplier<String> sup2;

		private Recomputer2(final Supplier<String> sup1, final Supplier<String> sup2)
		{
			this.sup1 = checkNotNull(sup1);
			this.sup2 = checkNotNull(sup2);
		}

		public String compute()
		{
			return sup1.get() + ";" + sup2.get();
		}
	}

	@Test
	public void supplier_changeEvent_notPropagated_throughBifunction() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> dep1 = gb.mutable("a");
		final MutableProvider<String> dep2 = gb.mutable("b");
		final Dep<Recomputer2> sut = gb.oblivious2((s, t) -> {
			count.incrementAndGet();
			return new Recomputer2(s, t);
		}, gb.supplier(dep1), gb.supplier(dep2));
		assertThat(count.get(), is(1));
		assertThat(sut.get().compute(), is("a;b"));
		assertThat(count.get(), is(1));
		dep1.set("aa");
		assertThat(count.get(), is(1));
		assertThat(sut.get().compute(), is("aa;b"));
		assertThat(count.get(), is(1));
		dep2.set("bb");
		assertThat(count.get(), is(1));
		assertThat(sut.get().compute(), is("aa;bb"));
		assertThat(count.get(), is(1));
	}

	private static final class ImmutableSupplier implements NotifiableSupplier<String>
	{
		private final String value;

		private ImmutableSupplier(final String value)
		{
			this.value = checkNotNull(value);
		}

		@Override
		public String get()
		{
			return value + "!";
		}

		@Override
		public boolean signal()
		{
			return false;
		}
	}

	@Test
	public void install_immutable_canGet() throws Exception
	{
		final Dep<String> dep = gb.singleton("xyz");
		final Dep<String> sut = gb.install(ImmutableSupplier::new, dep);
		assertThat(sut.get(), is("xyz!"));
	}

	@Test
	public void install_immutable_canRebuild() throws Exception
	{
		final MutableProvider<String> dep = gb.mutable("xyz");
		final Dep<String> sut = gb.install(ImmutableSupplier::new, dep);
		assertThat(sut.get(), is("xyz!"));
		dep.set("zyx");
		assertThat(sut.get(), is("zyx!"));
	}

	private static final class ForwardingSupplier implements NotifiableSupplier<String>
	{
		private final Supplier<String> sup;

		private ForwardingSupplier(final Supplier<String> sup)
		{
			this.sup = checkNotNull(sup);
		}

		@Override
		public String get()
		{
			return sup.get() + "!";
		}

		@Override
		public boolean signal()
		{
			return true;
		}
	}

	@Test
	public void install_forwarding_canGet() throws Exception
	{
		final Dep<String> value = gb.singleton("xyz");
		final Dep<String> sut = gb.install(ForwardingSupplier::new, gb.supplier(value));
		assertThat(sut.get(), is("xyz!"));
	}

	@Test
	public void install_forwarding_canRebuild() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<Supplier<String>> dep = gb.mutable(() -> "xyz");
		final Dep<String> sut = gb.install(x -> {
			count.incrementAndGet();
			return new ForwardingSupplier(x);
		}, dep);
		assertThat(count.get(), is(1));
		assertThat(sut.get(), is("xyz!"));
		assertThat(count.get(), is(1));
		dep.set(() -> "zyx");
		assertThat(count.get(), is(2));
		assertThat(sut.get(), is("zyx!"));
		assertThat(count.get(), is(2));
	}

	@Test
	public void install_forwarding_canChange_withoutRebuild() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("xyz");
		final Dep<String> sut = gb.install(x -> {
			count.incrementAndGet();
			return new ForwardingSupplier(x);
		}, gb.supplier(value));
		assertThat(count.get(), is(1));
		assertThat(sut.get(), is("xyz!"));
		assertThat(count.get(), is(1));
		value.set("zyx");
		assertThat(count.get(), is(1));
		assertThat(sut.get(), is("zyx!"));
		assertThat(count.get(), is(1));
	}

	@Test
	public void install_forwarding_rebuild_notifiesDownstream() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<Supplier<String>> dep = gb.mutable(() -> "xyz");
		final Dep<String> sut = gb.install(x -> {
			count.incrementAndGet();
			return new ForwardingSupplier(x);
		}, dep);
		final Dep<String> fn = gb.oblivious(x -> "f[" + x + "]", sut);
		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[xyz!]"));
		assertThat(count.get(), is(1));
		dep.set(() -> "zyx");
		assertThat(count.get(), is(2));
		assertThat(fn.get(), is("f[zyx!]"));
		assertThat(count.get(), is(2));
	}

	@Test
	public void install_forwarding_minorChange_notifiesDownstream() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("xyz");
		final Dep<String> sut = gb.install(x -> {
			count.incrementAndGet();
			return new ForwardingSupplier(x);
		}, gb.supplier(value));
		final Dep<String> fn = gb.oblivious(x -> "f[" + x + "]", sut);
		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[xyz!]"));
		assertThat(count.get(), is(1));
		value.set("zyx");
		assertThat(count.get(), is(1));
		assertThat(fn.get(), is("f[zyx!]"));
		assertThat(count.get(), is(1));
	}

	private interface TwoHalvesSupplier
	{
		String firstHalf();

		String secondHalf();
	}

	private static final class ChoppingAdaptor implements TwoHalvesSupplier
	{
		private final Supplier<String> supplier;

		public ChoppingAdaptor(final Supplier<String> supplier)
		{
			this.supplier = checkNotNull(supplier);
		}

		@Override
		public String firstHalf()
		{
			final String string = supplier.get();
			return string.substring(0, string.length() / 2);
		}

		@Override
		public String secondHalf()
		{
			final String string = supplier.get();
			return string.substring(string.length() / 2);
		}
	}

	private static final class SwappingAdaptor implements TwoHalvesSupplier
	{
		private final TwoHalvesSupplier supplier;

		public SwappingAdaptor(final TwoHalvesSupplier supplier)
		{
			this.supplier = checkNotNull(supplier);
		}

		@Override
		public String firstHalf()
		{
			return supplier.secondHalf();
		}

		@Override
		public String secondHalf()
		{
			return supplier.firstHalf();
		}
	}

	@Test
	public void supplierAdaptor_canGet() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("SomeText");
		final Dep<TwoHalvesSupplier> sut = gb.oblivious(s -> {
			count.incrementAndGet();
			return new ChoppingAdaptor(s);
		}, gb.supplier(value));

		assertThat(count.get(), is(1));
		assertThat(sut.get().firstHalf(), is("Some"));
		assertThat(sut.get().secondHalf(), is("Text"));
		assertThat(count.get(), is(1));
	}

	@Test
	public void supplierAdaptor_canChain() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("SomeText");
		final Dep<TwoHalvesSupplier> sut = gb.oblivious(s -> {
			count1.incrementAndGet();
			return new ChoppingAdaptor(s);
		}, gb.supplier(value));
		final Dep<TwoHalvesSupplier> result = gb.oblivious(s -> {
			count2.incrementAndGet();
			return new SwappingAdaptor(s);
		}, sut);

		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		assertThat(result.get().firstHalf(), is("Text"));
		assertThat(result.get().secondHalf(), is("Some"));
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
	}

	@Test
	public void supplierAdaptor_chain_canModify_withoutRebuild() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("SomeText");
		final Dep<TwoHalvesSupplier> sut = gb.oblivious(s -> {
			count1.incrementAndGet();
			return new ChoppingAdaptor(s);
		}, gb.supplier(value));
		final Dep<TwoHalvesSupplier> result = gb.oblivious(s -> {
			count2.incrementAndGet();
			return new SwappingAdaptor(s);
		}, sut);

		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		value.set("OtherTxt");
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		assertThat(result.get().firstHalf(), is("rTxt"));
		assertThat(result.get().secondHalf(), is("Othe"));
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
	}

	@Test
	public void supplierAdaptor_chain_canRebuild() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);
		final MutableProvider<Supplier<String>> sup = gb.mutable(() -> "SomeText");
		final Dep<TwoHalvesSupplier> sut = gb.oblivious(s -> {
			count1.incrementAndGet();
			return new ChoppingAdaptor(s);
		}, sup);
		final Dep<TwoHalvesSupplier> result = gb.oblivious(s -> {
			count2.incrementAndGet();
			return new SwappingAdaptor(s);
		}, sut);

		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		sup.set(() -> "NewValue");
		assertThat(count1.get(), is(2));
		assertThat(count2.get(), is(2));
		assertThat(result.get().firstHalf(), is("alue"));
		assertThat(result.get().secondHalf(), is("NewV"));
		assertThat(count1.get(), is(2));
		assertThat(count2.get(), is(2));
	}

	private static final class EachHalfAdaptor implements TwoHalvesSupplier
	{
		private final Supplier<String> a;
		private final Supplier<String> b;

		public EachHalfAdaptor(final Supplier<String> a, final Supplier<String> b)
		{
			this.a = checkNotNull(a);
			this.b = checkNotNull(b);
		}

		@Override
		public String firstHalf()
		{
			return "1=" + a.get();
		}

		@Override
		public String secondHalf()
		{
			return "2=" + b.get();
		}
	}

	@Test
	public void supplierAdaptor2_chain_canModify_withoutRebuild() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);
		final MutableProvider<String> value1 = gb.mutable("Value 1");
		final MutableProvider<String> value2 = gb.mutable("Value 2");
		final Dep<TwoHalvesSupplier> sut = gb.oblivious2((s, t) -> {
			count1.incrementAndGet();
			return new EachHalfAdaptor(s, t);
		}, gb.supplier(value1), gb.supplier(value2));
		final Dep<TwoHalvesSupplier> result = gb.oblivious(s -> {
			count2.incrementAndGet();
			return new SwappingAdaptor(s);
		}, sut);

		assertThat(result.get().firstHalf(), is("2=Value 2"));
		assertThat(result.get().secondHalf(), is("1=Value 1"));
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		value1.set("New value 1");
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		assertThat(result.get().firstHalf(), is("2=Value 2"));
		assertThat(result.get().secondHalf(), is("1=New value 1"));
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		value2.set("New value 2");
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		assertThat(result.get().firstHalf(), is("2=New value 2"));
		assertThat(result.get().secondHalf(), is("1=New value 1"));
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
	}

	private <T> Dep<Supplier<T>> escalate(final Dep<Supplier<T>> dep)
	{
		return gb.install(x -> new NotifiableSupplier<Supplier<T>>() {

			@Override
			public Supplier<T> get()
			{
				return x;
			}

			@Override
			public boolean signal()
			{
				return true;
			}
		}, dep);
	}

	@Test
	public void supplierAdaptor_escalate_causesRebuild() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("SomeText");
		final Dep<Supplier<String>> supplier = gb.supplier(value);
		final Dep<TwoHalvesSupplier> sut = gb.oblivious(s -> {
			count1.incrementAndGet();
			return new ChoppingAdaptor(s);
		}, escalate(supplier));
		final Dep<TwoHalvesSupplier> result = gb.oblivious(s -> {
			count2.incrementAndGet();
			return new SwappingAdaptor(s);
		}, sut);

		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		value.set("NewValue");
		assertThat(count1.get(), is(2));
		assertThat(count2.get(), is(2));
		assertThat(result.get().firstHalf(), is("alue"));
		assertThat(result.get().secondHalf(), is("NewV"));
	}

	@Test
	public void bifunction_escalateFirstSide_causesRebuild() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("text");
		final Dep<Supplier<String>> supplier = gb.supplier(value);
		final Dep<TwoHalvesSupplier> sut = gb.oblivious2((s, t) -> {
			count1.incrementAndGet();
			return new EachHalfAdaptor(s, t);
		}, escalate(supplier), supplier);
		final Dep<TwoHalvesSupplier> result = gb.oblivious(s -> {
			count2.incrementAndGet();
			return new SwappingAdaptor(s);
		}, sut);

		assertThat(result.get().firstHalf(), is("2=text"));
		assertThat(result.get().secondHalf(), is("1=text"));
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		value.set("new");
		assertThat(count1.get(), is(2));
		assertThat(count2.get(), is(2));
		assertThat(result.get().firstHalf(), is("2=new"));
		assertThat(result.get().secondHalf(), is("1=new"));
	}

	@Test
	public void bifunction_escalateSecondSide_causesRebuild() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("text");
		final Dep<Supplier<String>> supplier = gb.supplier(value);
		final Dep<TwoHalvesSupplier> sut = gb.oblivious2((s, t) -> {
			count1.incrementAndGet();
			return new EachHalfAdaptor(s, t);
		}, supplier, escalate(supplier));
		final Dep<TwoHalvesSupplier> result = gb.oblivious(s -> {
			count2.incrementAndGet();
			return new SwappingAdaptor(s);
		}, sut);

		assertThat(result.get().firstHalf(), is("2=text"));
		assertThat(result.get().secondHalf(), is("1=text"));
		assertThat(count1.get(), is(1));
		assertThat(count2.get(), is(1));
		value.set("new");
		assertThat(count1.get(), is(2));
		assertThat(count2.get(), is(2));
		assertThat(result.get().firstHalf(), is("2=new"));
		assertThat(result.get().secondHalf(), is("1=new"));
	}

	private static class Reverser implements Notifiable
	{
		private final Supplier<String> supplier;
		private int notificationCount;

		public Reverser(final Supplier<String> supplier)
		{
			this.supplier = checkNotNull(supplier);
			this.notificationCount = 0;
		}

		public String getReversedValue()
		{
			final StringBuilder sb = new StringBuilder();
			final String string = supplier.get();
			for (int i = string.length() - 1; i >= 0; i--)
			{
				sb.append(string.charAt(i));
			}
			return sb.toString();
		}

		@Override
		public boolean signal()
		{
			notificationCount++;
			return true;
		}
	}

	@Test
	public void constructor_majorEvent_getsRebuilt() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("text");
		final Dep<Reverser> sut = gb.construct(s -> {
			count.incrementAndGet();
			return new Reverser(s);
		}, escalate(gb.supplier(value)));

		assertThat(count.get(), is(1));
		assertThat(sut.get().notificationCount, is(0));
		assertThat(sut.get().getReversedValue(), is("txet"));

		value.set("thing");
		assertThat(count.get(), is(2));
		assertThat(sut.get().notificationCount, is(0));
		assertThat(sut.get().getReversedValue(), is("gniht"));
	}

	@Test
	public void constructor_minorEvent_notRebuilt_butNotified() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("text");
		final Dep<Reverser> sut = gb.construct(s -> {
			count.incrementAndGet();
			return new Reverser(s);
		}, gb.supplier(value));

		assertThat(count.get(), is(1));
		assertThat(sut.get().notificationCount, is(0));
		assertThat(sut.get().getReversedValue(), is("txet"));

		value.set("yay");
		assertThat(count.get(), is(1));
		assertThat(sut.get().notificationCount, is(1));
		assertThat(sut.get().getReversedValue(), is("yay"));
	}

	private static class FirstThree implements Notifiable, Supplier<String>
	{
		private final Supplier<String> supplier;
		private String value;
		private int notificationCount;

		public FirstThree(final Supplier<String> supplier)
		{
			this.supplier = checkNotNull(supplier);
			this.notificationCount = 0;
			this.value = valueFor(supplier.get());
		}

		private String valueFor(final String string)
		{
			if (string.length() <= 3)
			{
				return string;
			}
			else
			{
				return string.substring(0, 3);
			}
		}

		@Override
		public String get()
		{
			return value;
		}

		@Override
		public boolean signal()
		{
			notificationCount++;
			final String newValue = valueFor(supplier.get());
			final boolean changed = !newValue.equals(value);
			value = newValue;
			return changed;
		}
	}

	@Test
	public void constructor_minorEvent_whenTrue_thenPropagated() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("text");
		final Dep<FirstThree> sut = gb.construct(s -> {
			return new FirstThree(s);
		}, gb.supplier(value));
		final Dep<Reverser> result = gb.construct(s -> {
			count.incrementAndGet();
			return new Reverser(s);
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(sut.get().notificationCount, is(0));
		assertThat(result.get().notificationCount, is(0));
		assertThat(result.get().getReversedValue(), is("xet"));

		value.set("blah");
		assertThat(count.get(), is(1));
		assertThat(sut.get().notificationCount, is(1));
		assertThat(result.get().notificationCount, is(1));
		assertThat(result.get().getReversedValue(), is("alb"));
	}

	@Test
	public void constructor_minorEvent_whenFalse_thenNotPropagated() throws Exception
	{
		final AtomicInteger count = new AtomicInteger(0);
		final MutableProvider<String> value = gb.mutable("text");
		final Dep<FirstThree> sut = gb.construct(s -> {
			return new FirstThree(s);
		}, gb.supplier(value));
		final Dep<Reverser> result = gb.construct(s -> {
			count.incrementAndGet();
			return new Reverser(s);
		}, sut);

		assertThat(count.get(), is(1));
		assertThat(sut.get().notificationCount, is(0));
		assertThat(result.get().notificationCount, is(0));
		assertThat(result.get().getReversedValue(), is("xet"));

		value.set("texi");
		assertThat(count.get(), is(1));
		assertThat(sut.get().notificationCount, is(1));
		assertThat(result.get().notificationCount, is(0));
		assertThat(result.get().getReversedValue(), is("xet"));
	}

	private static final class Originator implements Notifiable, Supplier<String>
	{
		private final EventOrigin event;
		private String state;
		private int notificationCount;

		public Originator(final EventOrigin event)
		{
			this.event = checkNotNull(event);
			this.state = "Initial";
			this.notificationCount = 0;
		}

		public void setValue(final String state)
		{
			this.state = state;
			event.fire();
		}

		@Override
		public String get()
		{
			return state;
		}

		@Override
		public boolean signal()
		{
			notificationCount++;
			return true;
		}
	}

	@Test
	public void eventSource_whenInjected_andFired_thenTriggersEvent_butNotRebuild() throws Exception
	{
		final AtomicInteger count1 = new AtomicInteger(0);
		final AtomicInteger count2 = new AtomicInteger(0);

		final Dep<Originator> sut = gb.construct(x -> {
			count1.incrementAndGet();
			return new Originator(x);
		}, gb.eventSource());

		final Dep<Reverser> reverse = gb.construct(x -> {
			count2.incrementAndGet();
			return new Reverser(x);
		}, sut);

		assertThat(reverse.get().getReversedValue(), is("laitinI"));
		assertThat(sut.get().notificationCount, is(0));

		sut.get().setValue("New thing");
		assertThat(sut.get().notificationCount, is(1));
		assertThat(reverse.get().getReversedValue(), is("gniht weN"));
	}
}
