package org.xhy.filter;

public interface Filter<T> {

    FilterResponse doFilter(FilterData<T> filterData);

}
