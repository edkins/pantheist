package restless.common.util;

/**
 * I've separated this out into two interfaces just to make it clearer what's going on.
 */
public interface MakeList<T, R> extends MakeListBuilder<T, R>, MakeListApply<T, R>
{

}
