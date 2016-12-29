public class ThreadDemo extends Thread {  
      
    private String name;  
    public ThreadDemo(String name){  
        this.name = name;  
    }  
      
    public void run(){  
        for(int i = 0; i < 9; i++){  
            System.out.println(name+"..........i="+i);  
        }  
    }  
      
    public static void main(String[] args) {  
        ThreadDemo demo = new ThreadDemo("李四");  
        ThreadDemo demo1 = new ThreadDemo("张三");  
        demo.start();  
        demo1.start();  
    }  
}  