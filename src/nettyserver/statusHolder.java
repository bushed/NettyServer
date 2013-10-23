package nettyserver;

/**
 *
 * @author Gribakin O
 */
class StatusHolder {
    private int count = 0;
    /**
     * @return current value
     */
    public synchronized int count() {
        return count;
    }

    /**
     * increment value by 1
     */
    public synchronized void add() {
        count++;
    }
    
    /**
     * decrement value by 1
     */
    public synchronized void remove(){
        count--;
    }    
    
}
