import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class MiniCrawler {
	
	private Set<String> alreadyCrawledSet;
	private Queue<String> unCrawlQueue;
	private static Set<String> picSrcSet;
	private static final int MAX_COUNT = 10;
	private String newFolder;
	
	
	public MiniCrawler()
	{
		this.alreadyCrawledSet = new HashSet();
		this.unCrawlQueue = new ArrayDeque();
		this.picSrcSet = new HashSet<String>();
		
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		
	    String folder = month+"_"+day+"_"+hour+"_"+minute+"_"+second;  
		newFolder = "D:\\"+folder+"\\";
		File dir = new File(newFolder);
		if( null == dir || !dir.exists())
		{
			dir.mkdir();
		}
	}
	
	public void crawl(String beginUrl)
	{
		Parser parser = null;
		try {
				parser = new Parser(beginUrl);
				parser.setEncoding(parser.getEncoding());
				NodeList nodeList = parser.parse(null);
				parseNodeList(nodeList);
		}
		catch(ParserException e)
		{
				e.printStackTrace();
		}
		alreadyCrawledSet.add(beginUrl);
			//已经爬完或者已经爬了最大网页数
		while(!unCrawlQueue.isEmpty() && alreadyCrawledSet.size()< MAX_COUNT)
		{
			String newUrl = unCrawlQueue.remove();
			try{
					parser.setResource(newUrl);
					parser.setEncoding(parser.getEncoding());
					NodeList nl = parser.parse(null);
					parseNodeList(nl);
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			alreadyCrawledSet.add(newUrl);
		}
		System.out.println("Crawl Finish!");
		
		FileOutputStream fos = null;
		PrintWriter pw = null;
		//把得到的全部图片地址写到文件中
		try {
			String picFile = newFolder+"picAddrs.txt";
			fos = new FileOutputStream(new File(picFile));
			pw = new PrintWriter(fos);
			Iterator<String> iter = picSrcSet.iterator();
			while(iter.hasNext())
			{
				String str = iter.next().toString();
				pw.write(str);
				pw.flush();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			if(null!=pw)
			{
				pw.close();
				pw = null;
			}
		}
		System.out.println("====== All the pic address have been writen to the file!");
	}
	
	public void parseNodeList(NodeList nodeList)
	{
		NodeIterator iter = nodeList.elements();
		try {
			while(iter.hasMoreNodes())
			{
				Node node = iter.nextNode();
				if(node.getClass() == LinkTag.class)
				{
					LinkTag tag = (LinkTag)node;
					String href = tag.getLink();
					//System.out.println("===find a link: "+href);
					if(null != href)
					{
						if(!alreadyCrawledSet.contains(href) && (href.indexOf("www.169pp.com")!=-1) && href.endsWith("htm"))
						{
							System.out.println("----find link: "+href);
							unCrawlQueue.add(href);
						}
					}
				}
				else
				{
					NodeList childList = node.getChildren();
					if( childList == null)
					{
						//说明是末尾节点，打印
						//System.out.println("class: "+node.getClass()+",text: "+node.getText());
						if(node instanceof ImageTag)
						{
							ImageTag tag = (ImageTag)node;
							String picURL = tag.getImageURL();
							if(picURL.endsWith(".jpg")||picURL.endsWith(".jpeg"))//||picURL.endsWith(".gif"))
							{
								picSrcSet.add(picURL);
							}
						}
					}
					else
					{
						parseNodeList(childList);
					}
					
				}
				
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void displayAlreadyCrawlUrl()
	{
		System.out.println("=================================");
		Iterator<String> iter = alreadyCrawledSet.iterator();
		while(iter.hasNext())
		{
			System.out.println(iter.next());
		}
		
		System.out.println("=================================");
		Iterator<String> picIter = picSrcSet.iterator();
		while(picIter.hasNext())
		{
			System.out.println(picIter.next());
		}
		
	}
	
	
	public void download(Set<String> set,int flag)
	{
		if(null == set)
		{
			return;
		}
		System.out.println("They are total "+set.size()+" pics need to be download");
		Iterator<String> iter = set.iterator();
		int i = 0;
		while(iter.hasNext())
		{
			String sourceFile = iter.next();
			String newFileName = newFolder;
			if(sourceFile.endsWith(".jpg"))
			{
				String s = i+".jpg";
				newFileName += s;
			}
			else if(sourceFile.endsWith(".jpeg"))
			{
				String s = i+".jpeg";
				newFileName += s;
			}
			i++;
			URL url = null;
			try {
				url = new URL(sourceFile);
				URLConnection conn = url.openConnection();
				InputStream inputStream = conn.getInputStream();
				FileOutputStream outputStream = new FileOutputStream(new File(newFileName));
				byte[] bytes = new byte[1024];
				int len = 0;
				while((len = inputStream.read(bytes))!=-1)
				{
					outputStream.write(bytes,0,len);
				}
				
				outputStream.close();
				inputStream.close();
				
				System.out.println(i+" download finished");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
		System.out.println("---------All Download Finished!");
		
	}
	
	public static void main(String[] args)
	{
		try{
			String beginUrl = "http://www.169pp.com/";
			MiniCrawler crawler = new MiniCrawler();
			crawler.crawl(beginUrl);
			//parser.displayAlreadyCrawlUrl();
			crawler.download(picSrcSet,0);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}