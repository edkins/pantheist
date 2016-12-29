package restless.common.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class MakeTest
{
	@Test
	public void list_noop() throws Exception
	{
		final List<Integer> myList = ImmutableList.of(1, 2, 3);

		final List<Integer> result = Make.<Integer>list().from(myList);

		assertThat(result, is(myList));
	}

	@Test
	public void list_appendLast() throws Exception
	{
		final List<Integer> myList = ImmutableList.of(1, 2, 3);

		final List<Integer> result = Make.<Integer>list().from(myList, 4);

		assertThat(result, is(ImmutableList.of(1, 2, 3, 4)));
	}

	@Test
	public void list_appendMultiple() throws Exception
	{
		final List<Integer> myList = ImmutableList.of(1, 2, 3);

		final List<Integer> result = Make.<Integer>list().withLast(5).withLast(4).from(myList);

		assertThat(result, is(ImmutableList.of(1, 2, 3, 4, 5)));
	}

	@Test
	public void optional_emptyList() throws Exception
	{
		final Optional<Integer> result = Make.<Integer>failIfMultiple().from(ImmutableList.of());

		assertThat(result, is(Optional.empty()));
	}

	@Test
	public void optional_singletonList() throws Exception
	{
		final Optional<Integer> result = Make.<Integer>failIfMultiple().from(ImmutableList.of(7));

		assertThat(result, is(Optional.of(7)));
	}

	@Test(expected = IllegalStateException.class)
	public void optional_failIfMultiple() throws Exception
	{
		Make.<Integer>failIfMultiple().from(ImmutableList.of(7, 8));
	}

	@Test
	public void single_theOnly() throws Exception
	{
		final Integer result = Make.<Integer>theOnly().from(ImmutableList.of(7));

		assertThat(result, is(7));
	}

	@Test(expected = IllegalStateException.class)
	public void tail_failIfEmpty() throws Exception
	{
		Make.<Integer>list().tail().from(ImmutableList.of());
	}

	@Test
	public void tail_singleton() throws Exception
	{
		final List<Integer> result = Make.<Integer>list().tail().from(ImmutableList.of(123));

		assertThat(result, is(ImmutableList.of()));
	}

	@Test
	public void tail_two() throws Exception
	{
		final List<Integer> result = Make.<Integer>list().tail().from(ImmutableList.of(123, 124));

		assertThat(result, is(ImmutableList.of(124)));
	}

	@Test(expected = IllegalStateException.class)
	public void init_failIfEmpty() throws Exception
	{
		Make.<Integer>list().init().from(ImmutableList.of());
	}

	@Test
	public void init_singleton() throws Exception
	{
		final List<Integer> result = Make.<Integer>list().init().from(ImmutableList.of(123));

		assertThat(result, is(ImmutableList.of()));
	}

	@Test
	public void init_two() throws Exception
	{
		final List<Integer> result = Make.<Integer>list().init().from(ImmutableList.of(123, 124));

		assertThat(result, is(ImmutableList.of(123)));
	}

	@Test
	public void init_several() throws Exception
	{
		final List<Integer> result = Make.<Integer>list().init().from(ImmutableList.of(2, 3, 5, 7, 11, 13, 17));

		assertThat(result, is(ImmutableList.of(2, 3, 5, 7, 11, 13)));
	}

	@Test
	public void snowball() throws Exception
	{
		final int result = Make.<Integer>single()
				.<Integer>snowball(100, (x, y) -> x - y)
				.from(ImmutableList.of(1, 2, 3));

		assertThat(result, is(94));
	}

	@Test
	public void join_empty() throws Exception
	{
		final Optional<String> result = Make.join("%").from(ImmutableList.of());

		assertThat(result, is(Optional.empty()));
	}

	@Test
	public void join_single() throws Exception
	{
		final Optional<String> result = Make.join("%").from(ImmutableList.of("abc"));

		assertThat(result, is(Optional.of("abc")));
	}

	@Test
	public void join_several() throws Exception
	{
		final Optional<String> result = Make.join("%").from(ImmutableList.of("a", "b", "c"));

		assertThat(result, is(Optional.of("a%b%c")));
	}

	@Test(expected = IllegalStateException.class)
	public void drop_fewer_fails() throws Exception
	{
		Make.<Integer>list().drop(3).from(ImmutableList.of(100, 101));
	}

	@Test
	public void drop_sameNumber() throws Exception
	{
		final List<Integer> result = Make.<Integer>list().drop(3).from(ImmutableList.of(100, 101, 102));

		assertThat(result, is(ImmutableList.of()));
	}

	@Test
	public void drop_more() throws Exception
	{
		final List<Integer> result = Make.<Integer>list().drop(3).from(ImmutableList.of(100, 101, 102, 103, 104));

		assertThat(result, is(ImmutableList.of(103, 104)));
	}
}
