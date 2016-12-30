package restless.common.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class AntiIteratorTest
{
	private <T> AntiIterator<T> these(@SuppressWarnings("unchecked") final T... items)
	{
		return AntiIt.array(items);
	}

	private <T> void assertYield(final AntiIterator<T> ait, @SuppressWarnings("unchecked") final T... items)
	{
		assertThat(ait.toList(), is(ImmutableList.copyOf(items)));
	}

	private <T> void assertEmptyYield(final AntiIterator<T> ait)
	{
		assertThat(ait.toList(), is(ImmutableList.of()));
	}

	@Test
	public void join_empty() throws Exception
	{
		assertThat(AntiIt.empty().join("%"), is(Optional.empty()));
	}

	@Test
	public void join_one() throws Exception
	{
		assertThat(AntiIt.single("x").join("%").get(), is("x"));
	}

	@Test
	public void join_several() throws Exception
	{
		assertThat(these("x", "yyy", "z").join("%").get(), is("x%yyy%z"));
	}

	@Test(expected = ClassCastException.class)
	public void join_wrongClass() throws Exception
	{
		AntiIt.single(17).join("%");
	}

	@Test(expected = IllegalStateException.class)
	public void init_empty() throws Exception
	{
		AntiIt.empty().init().toList();
	}

	@Test
	public void init_one() throws Exception
	{
		assertEmptyYield(these(123).init());
	}

	@Test
	public void init_two() throws Exception
	{
		assertYield(these("abc", "def").init(), "abc");
	}

	@Test
	public void init_three() throws Exception
	{
		assertYield(these("abc", "def", "ghi").init(), "abc", "def");
	}

	@Test
	public void split_empty() throws Exception
	{
		assertYield(AntiIt.split('$', ""), "");
	}

	@Test
	public void split_delim() throws Exception
	{
		assertYield(AntiIt.split('$', "$"), "", "");
	}

	@Test
	public void split_x() throws Exception
	{
		assertYield(AntiIt.split('$', "x"), "x");
	}

	@Test
	public void split_delim_x() throws Exception
	{
		assertYield(AntiIt.split('$', "$xx"), "", "xx");
	}

	@Test
	public void split_x_delim() throws Exception
	{
		assertYield(AntiIt.split('$', "x$"), "x", "");
	}

	@Test
	public void split_usual() throws Exception
	{
		assertYield(AntiIt.split(',', "a,bcd,e,f"), "a", "bcd", "e", "f");
	}

	@Test
	public void split_empty_segment() throws Exception
	{
		assertYield(AntiIt.split(',', "a,bcd,,f"), "a", "bcd", "", "f");
	}

	@Test(expected = IllegalStateException.class)
	public void drop_fewer_fails() throws Exception
	{
		these(100, 101).drop(3, true).toList();
	}

	@Test
	public void drop_sameNumber() throws Exception
	{
		assertEmptyYield(these(100, 101, 102).drop(3, true));
	}

	@Test
	public void drop_more() throws Exception
	{
		assertYield(these(100, 101, 102, 103, 104).drop(3, true), 103, 104);
	}
}
