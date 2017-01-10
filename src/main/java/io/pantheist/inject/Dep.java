package io.pantheist.inject;

import java.util.function.Supplier;

interface Dep<T> extends Supplier<T>, EventNode
{
}
