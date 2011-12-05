package tu.space.utils;

public enum EnumComponent {
	
	//elem
	CPU("cpu", 0),
	GPU("gpu", 1),
	RAM("ram", 2),
	MAINBOARD("mainboard", 3);
	
	//fields
	private String name;
	private int order;
	
	private EnumComponent(String name, int order){
		this.name = name;
		this.order = order;
	}
	
	public String getName(){
		return name;
	}
	
	public int getOrder(){
		return order;
	}
}
