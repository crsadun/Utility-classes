package org.sadun.util.tp;

class FIFOQueue implements Queue {

    private com.deltax.util.FIFOQueue q = new com.deltax.util.FIFOQueue();

    public void put(Runnable obj) { q.put(obj); }
    public Runnable get() {
        try {
            return (Runnable)q.get();
        } catch(com.deltax.util.QueueEmptyException e) {
            return null;
        }
    }

    public int size() { return q.size(); }
    public boolean isEmpty()  { return q.isEmpty(); }
}