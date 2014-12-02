package urChatBasic;

public class IRCUser implements Comparable<IRCUser>{
	private String name;

	public IRCUser(String name){
		this.name = name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String toString(){
		return this.name;
	}
	
	@Override
	public int compareTo(IRCUser comparison) {
		return name.compareTo(comparison.getName());
	}

}
