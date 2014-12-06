package urChatBasic;

public class IRCUser implements Comparable<IRCUser>{
	private String name;
	private String userStatus = "";
	
	public IRCUser(String name){
		this.name = name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name.replace(userStatus,"");
	}
	
	@Override
	public String toString(){
		return this.name;
	}
	
	public String getUserStatus(){
		return this.userStatus;
	}
	
	public void setUserStatus(String c){
		userStatus = c;
	}
	

	
	@Override
	public int compareTo(IRCUser comparison) {
		return name.compareTo(comparison.getName());
	}

}
