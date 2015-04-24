package com.pafc.library.util;


import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class WeakList<T> implements List<T> {

    private List<WeakReferenceObject<T>> mWeakList;

    private WeakList() {

    }

    public static <T> WeakList<T> newWeakArrayList() {
        final WeakList<T> list = new WeakList<>();
        list.mWeakList = new ArrayList<>();
        return list;
    }

    public static <T> WeakList<T> newWeakLinkedList() {
        final WeakList<T> list = new WeakList<>();
        list.mWeakList = new LinkedList<>();
        return list;
    }

    @Override
    public void add(int location, T object) {
        mWeakList.add(location, new WeakReferenceObject<>(object));
    }

    @Override
    public boolean add(T object) {
        return mWeakList.add(new WeakReferenceObject<>(object));
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        return mWeakList.addAll(location, createWeakConnection(collection));
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return mWeakList.addAll(createWeakConnection(collection));
    }

    private Collection<WeakReferenceObject<T>> createWeakConnection(Collection<? extends T> collection) {
        final Collection<WeakReferenceObject<T>> weakCollection = new ArrayList<>(collection.size());
        for (T item : collection) {
            weakCollection.add(new WeakReferenceObject<>(item));
        }
        return weakCollection;
    }

    private Collection<WeakReferenceObject<?>> createWeakConnectionWithoutType(Collection<?> collection) {
        final Collection<WeakReferenceObject<?>> weakCollection = new ArrayList<>(collection.size());
        for (Object item : collection) {
            weakCollection.add(new WeakReferenceObject<>(item));
        }
        return weakCollection;
    }

    @Override
    public void clear() {
        mWeakList.clear();
    }

    @Override
    public boolean contains(Object object) {
        return mWeakList.contains(new WeakReferenceObject<>(object));
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return mWeakList.containsAll(createWeakConnectionWithoutType(collection));
    }

    @Override
    public T get(int location) {
        return mWeakList.get(location).get();
    }

    @Override
    public int indexOf(Object object) {
        return mWeakList.indexOf(new WeakReferenceObject<>(object));
    }

    @Override
    public boolean isEmpty() {
        return mWeakList.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new WeakIterator(mWeakList);
    }

    @Override
    public int lastIndexOf(Object object) {
        return mWeakList.lastIndexOf(new WeakReferenceObject<>(object));
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return new WeakListIterator(mWeakList);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        return new WeakListIterator(mWeakList, location);
    }

    @Override
    public T remove(int location) {
        WeakReferenceObject<T> wo = mWeakList.remove(location);
        return wo == null ? null : wo.get();
    }

    @Override
    public boolean remove(Object object) {
        return mWeakList.remove(new WeakReferenceObject<>(object));
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return mWeakList.removeAll(createWeakConnectionWithoutType(collection));
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return mWeakList.retainAll(createWeakConnectionWithoutType(collection));
    }

    @Override
    public T set(int location, T object) {
        WeakReferenceObject<T> preItem = mWeakList.set(location, new WeakReferenceObject<>(object));
        return preItem == null ? null : preItem.get();
    }

    @Override
    public int size() {
        return mWeakList.size();
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
        return null;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(T1[] array) {
        return null;
    }


    private class WeakIterator implements Iterator<T> {

        private Iterator<WeakReferenceObject<T>> mDataIterator;


        private WeakIterator(List<WeakReferenceObject<T>> list) {
            mDataIterator = list.iterator();
        }

        @Override
        public boolean hasNext() {
            return mDataIterator.hasNext();
        }

        @Override
        public T next() {
            return mDataIterator.next().get();
        }

        @Override
        public void remove() {
            mDataIterator.remove();
        }
    }

    private class WeakListIterator implements ListIterator<T> {

        private ListIterator<WeakReferenceObject<T>> mDataIterator;

        private WeakListIterator(List<WeakReferenceObject<T>> list) {
            mDataIterator = list.listIterator();
        }

        private WeakListIterator(List<WeakReferenceObject<T>> list, int location) {
            mDataIterator = list.listIterator(location);
        }

        @Override
        public void add(T object) {
            mDataIterator.add(new WeakReferenceObject<>(object));
        }

        @Override
        public boolean hasNext() {
            return mDataIterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return mDataIterator.hasPrevious();
        }

        @Override
        public T next() {
            return mDataIterator.next().get();
        }

        @Override
        public int nextIndex() {
            return mDataIterator.nextIndex();
        }

        @Override
        public T previous() {
            return mDataIterator.previous().get();
        }

        @Override
        public int previousIndex() {
            return mDataIterator.previousIndex();
        }

        @Override
        public void remove() {
            mDataIterator.remove();
        }

        @Override
        public void set(T object) {
            mDataIterator.set(new WeakReferenceObject<>(object));
        }
    }

    private class WeakReferenceObject<T> extends WeakReference<T> {

        public WeakReferenceObject(T r) {
            super(r);
        }

        //override the method
        @Override
        public boolean equals(Object o) {
            if (o instanceof WeakReferenceObject) {
                final T obj1 = get();
                final Object obj2 = ((WeakReferenceObject) o).get();
                if (obj1 != null && obj2 != null) {
                    return obj1.equals(obj2);
                }
            }
            return false;
        }
    }

}
