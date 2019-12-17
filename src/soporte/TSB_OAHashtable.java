package soporte;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class TSB_OAHashtable<K, V> implements Map<K, V>, Cloneable, Serializable {

    private final static int MAX_SIZE = Integer.MAX_VALUE;
    private Entry<K, V> table[];
    private int initial_capacity;
    private int count;
    private float load_factor;

    protected transient int modCount;

    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K, V>> entrySet = null;
    private transient Collection<V> values = null;

    public TSB_OAHashtable() {
        this(11, 0.8f);
    }

    public TSB_OAHashtable(int initial_capacity) {
        if (!esPrimo(initial_capacity)) { initial_capacity = siguientePrimo(initial_capacity); }

        this.initial_capacity = initial_capacity;
        this.load_factor = 0.8f;
        this.count = 0;
        this.modCount = 0;
    }

    public TSB_OAHashtable(int initial_capacity, float load_factor) {
        if (load_factor <= 0) { load_factor = 0.8f; }
        if (initial_capacity <= 0) { initial_capacity = 11; }
        else {
            if (!esPrimo(initial_capacity)) {
                initial_capacity = siguientePrimo(initial_capacity);
            }
            if (initial_capacity > TSB_OAHashtable.MAX_SIZE) {
                initial_capacity = TSB_OAHashtable.MAX_SIZE;
            }
        }

        this.table = new Entry[initial_capacity];
        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;
    }

    public TSB_OAHashtable(Map<? extends K, ? extends V> t) {
        this(11, 0.8f);
        this.putAll(t);
    }

    private int siguientePrimo(int n) {
        if (n % 2 == 0) { n++; }
        while(!esPrimo(n)) n+=2;
        return n;
    }

    private boolean esPrimo(int n) {
        if (n%2==0) return false;
        for(int i=3; i*i<=n; i+=2) {
            if(n%i==0) return false;
        }
        return true;
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public boolean isEmpty() {
        return (this.count == 0);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.contains(value);
    }

    public boolean contains(Object value) {
        if (value == null) { return false; }
        for(int i = 0; i < this.table.length; ++i) {
            if (this.table[i] != null && this.table[i].getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean containsKey(Object key) {
        return (this.get((K) key) != null);
    }

    @Override
    public V get(Object key) {
        if (indiceValue(key) == -1) {
            return null;
        } else {
            return table[indiceValue(key)].getValue();
        }
    }

    private int indiceValue(Object key) {
        if (key == null) {
            throw new NullPointerException("get(): parámetro null");
        }
        int iMadre = h(key.hashCode());
        for (int j = 0;; j++) {
            int i = h(iMadre + j * j);
            if (table[i] == null) {
                return -1;
            }
            if ((table[i].key.equals(key)) && (!table[i].esTumba())) {
                return i;
            }
        }
    }

    @Override
    public V put(K key, V value) {
        if (key == null || value == null) { throw new NullPointerException("put(): parámetro null"); }

        if (count + 1 >= this.load_factor * this.table.length) { rehash(); }

        int iMadre = h(key), iTumba = -1;
        for (int i = 0; i < table.length; i++) {
            int j = h(iMadre + i * i);
            if (table[j] != null) {
                if (table[j].esTumba()) {
                    if (iTumba == -1) iTumba = j;
                } else {
                    if (table[j].getKey().equals(key)) {
                        V old = table[j].getValue();
                        table[j].setValue(value);
                        return old;
                    }
                }
            } else {
                if (iTumba == -1) {
                    table[j] = new Entry(key, value);
                    count++;
                    return null;
                } else {
                    table[iTumba] = new Entry(key, value);
                    count++;
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            throw new NullPointerException("remove(): parámetro null");
        }

        if (indiceValue(key) == -1) {
            return null;
        } else {
            V old = table[indiceValue(key)].getValue();
            table[indiceValue(key)].hacerTumba();
            count--;
            return old;
        }

    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        table = new Entry[initial_capacity];
        count = 0;
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new TSB_OAHashtable.KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new TSB_OAHashtable.ValueCollection();
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new TSB_OAHashtable.EntrySet();
        }
        return entrySet;
    }

    protected void rehash() {
        Entry<K, V>[] aux = table;
        table = new Entry[siguientePrimo(aux.length * 2 + 1)];
        count = 0;
        for (int i = 0; i < aux.length; i++) {
            if ((aux[i] != null) && (!aux[i].esTumba())) {
                put(aux[i].getKey(), aux[i].getValue());
            }
        }
    }

    public int hashCode() {
        if (this.isEmpty()) { return 0; }
        int hc = 0;
        for (Map.Entry<K, V> entry : this.entrySet()) {
            hc += entry.hashCode();
        }
        return hc;
    }

    public String toString() {
        StringBuilder cad = new StringBuilder("");
        for(int i = 0; i < this.table.length; i++)
        {
            if(table[i]!=null && !(table[i].esTumba())){
                cad.append(table[i].toString());
            }

        }
        return cad.toString();
    }

    protected Object clone() throws CloneNotSupportedException
    {
        TSB_OAHashtable<K, V> aux = (TSB_OAHashtable<K, V>) super.clone();
        aux.table = new Entry[this.table.length];
        System.arraycopy(this.table, 0, aux.table, 0, count);

        return aux;


        /*
        TSB_OAHashtable<K, V> t = (TSB_OAHashtable<K, V>)super.clone();
        t.table = new Entry[table.length];
        for (int i = table.length ; i-- > 0 ; )
        {
            t.table[i] = (Entry<K, V>) table[i].clone();
        }
        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;*/
    }

    public boolean equals(Object obj)
    {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext())
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) { return false; }
                else
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        }

        catch (ClassCastException | NullPointerException e)
        {
            return false;
        }

        return true;
    }

    private int h(int k) {
        return h(k, this.table.length);
    }

    private int h(K key) {
        return h(key.hashCode(), this.table.length);
    }

    private int h(K key, int t) {
        return h(key.hashCode(), t);
    }

    private int h(int k, int t) {
        if (k < 0) {
            k *= -1;
        }
        return k % t;
    }

    private class Entry<K, V> implements Map.Entry<K, V>, Serializable {

        private K key;
        private V value;

        public Entry(K key, V value) {
            if (key == null || value == null) {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
        }

        public boolean esTumba() {
            return value == null;
        }

        public void hacerTumba() {
            value = null;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            if (value == null) {
                throw new IllegalArgumentException("setValue(): parámetro null...");
            }

            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }

            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "(" + key.toString() + ", " + value.toString() + ")";
        }
    }

    private class KeySet extends AbstractSet<K> {

        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        public int size() {
            return count;
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            return (TSB_OAHashtable.this.remove(o)) != null;
        }

        public void clear() {
            TSB_OAHashtable.this.clear();
        }

        private class KeySetIterator implements Iterator<K> {

            private int current_entry;
            private boolean next_ok;

            public KeySetIterator() {
                current_entry = -1;
                next_ok = false;
            }

            public boolean hasNext() {

                if (isEmpty()) {
                    return false;
                }
                if (current_entry >= table.length) {
                    return false;
                }

                int aux = current_entry + 1;
                while ((aux < table.length) && ((table[current_entry] == null) || (table[this.current_entry].esTumba()))) {
                    aux++;
                }

                return (aux < table.length);
            }

            public K next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                current_entry++;
                while ((current_entry < table.length) && ((table[current_entry] == null) || (table[this.current_entry].esTumba()))) {
                    current_entry++;
                }
                next_ok = true;

                K key = table[current_entry].getKey();
                return key;
            }

            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }
                Entry<K, V> eliminado = table[current_entry];
                TSB_OAHashtable.this.remove(eliminado.getKey());
                current_entry--;
                while ((current_entry >= 0) && ((table[current_entry] == null) || (table[this.current_entry].esTumba()))) {
                    current_entry--;
                }
                next_ok = false;
            }
        }
    }

    private class ValueCollection extends AbstractCollection<V> {

        public Iterator<V> iterator() {
            return new ValueCollectionIterator();
        }

        public int size() {
            return count;
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public void clear() {
            TSB_OAHashtable.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V> {

            private int current_entry;
            private boolean next_ok;

            public ValueCollectionIterator() {
                current_entry = -1;
                next_ok = false;
            }

            public boolean hasNext() {
                if (isEmpty()) return false;
                if (current_entry >= table.length) return false;
                int aux = current_entry + 1;
                while ((aux < table.length) && ((table[aux] == null) || (table[aux].esTumba()))) {
                    aux++;
                }
                return (aux < table.length);
            }

            public V next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                current_entry++;
                while ((current_entry < table.length) && ((table[current_entry] == null) || (table[this.current_entry].esTumba()))) {
                    current_entry++;
                }
                next_ok = true;
                V value = table[current_entry].getValue();
                return value;
            }

            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                Entry<K, V> eliminado = table[current_entry];
                TSB_OAHashtable.this.remove(eliminado.getKey());
                current_entry -= 1;
                while ((current_entry < table.length) && ((table[current_entry] == null) || (table[this.current_entry].esTumba()))) {
                    current_entry--;
                }

                next_ok = false;
            }
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        public int size() {
            return count;
        }

        public void clear() {
            TSB_OAHashtable.this.clear();
        }

        public boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof TSB_OAHashtable.Entry)) {
                return false;
            }
            Entry<K, V> entry = (TSB_OAHashtable.Entry) o;
            K key = entry.getKey();
            return containsKey(key);
        }

        public boolean remove(Object o) {
            if (o == null) {
                throw new NullPointerException("remove(): parámetro null");
            }
            if (!(o instanceof TSB_OAHashtable.Entry)) {
                return false;
            }
            Entry<K, V> entry = (TSB_OAHashtable.Entry) o;
            K key = entry.getKey();
            return TSB_OAHashtable.this.remove(key) != null;
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>> {

            private int current_entry;
            private boolean next_ok;

            public EntrySetIterator() {
                current_entry = -1;
                next_ok = false;
            }

            public boolean hasNext() {
                if (isEmpty()) {
                    return false;
                }
                if (current_entry >= table.length) {
                    return false;
                }
                int aux = current_entry + 1;
                while ((aux < table.length) && ((table[aux] == null) || (table[aux].esTumba()))) {
                    aux++;
                }
                return aux < table.length;
            }

            public TSB_OAHashtable.Entry next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }
                current_entry++;
                while ((current_entry < table.length) && ((table[current_entry] == null) || (table[this.current_entry].esTumba()))) {
                    current_entry++;
                }

                next_ok = true;
                return table[current_entry];
            }

            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }
                Entry<K, V> eliminado = table[current_entry];
                TSB_OAHashtable.this.remove(eliminado.getKey());
                current_entry--;
                while ((current_entry < table.length) && ((table[current_entry] == null) || (table[this.current_entry].esTumba()))) {
                    current_entry--;
                }
                next_ok = false;
            }
        }
    }
}
