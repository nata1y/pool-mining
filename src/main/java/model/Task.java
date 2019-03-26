package model;

/**
 * Represents task on which miner is able to work.
 */
public class Task {
    
    /**
     * Integer that represents difficultiness of a task.
     */
    private int time;

    public Task(int t){
        this.time = t;
    }

    /**
     * Decrease what is left to work on.
     * 
     * @return false if task is completed.
     */
    public boolean work(){
        if(this.time > 0) {
            this.time--;
            return true;
        } else {
            return false;
        }
    }

    public int getTime() {
        return time;
    }
}
