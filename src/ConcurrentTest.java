import java.io.Reader;
import java.io.FileReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.*;
import java.util.List;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.*;


class MyCounter implements Runnable{
		private ConcurrentMap<String,Integer> map=null;
		private String line=null;
		public MyCounter(String line,ConcurrentMap<String,Integer> map){
			this.line=line;
			this.map=map;
		}
		@Override
		public void run(){
			for(String word:line.split(" ")){
				//System.out.println(word);
				increase(word);
			}
		}
		public int increase(String word){
			Integer oldValue;
			Integer newValue;
			while(true){
				oldValue=map.get(word);
				if(oldValue==null){
					newValue=1;
					if(map.putIfAbsent(word,newValue)==null)
						break;
				}
				else{
					newValue=oldValue+1;
					if(map.replace(word,oldValue,newValue))
						break;
				}
			}
			return newValue;
		}
}

public class ConcurrentTest{
	private static final ConcurrentMap<String,Integer> map=new ConcurrentHashMap<String,Integer>();
	public static void main(String[] args) throws IOException{
		Path p=Paths.get(args[0]);
		List<String> lines=Files.readAllLines(p,StandardCharsets.UTF_8);
		ExecutorService service=Executors.newFixedThreadPool(10);
		for(String line:lines)
		{
			//new Thread(new MyCounter(line,map)).start();
			service.execute(new MyCounter(line,map));
		}
		service.shutdown();
		while(!service.isTerminated())
			;
		Iterator i=map.keySet().iterator();
		
		while(i.hasNext()){
			String word=(String)i.next();
			int count=map.get(word);
			System.out.println(word + " " +count);
		}
	}
}
