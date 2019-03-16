package model;

public class Task {
    private int time;

    public Task(int t){
        this.time = t;
    }

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
