package restfulspring.view.tree;

public class MyInput {
	  private MyElement[] firstLevelElements;

	    public MyInput(MyElement[] firstLevelElements) {
	        this.firstLevelElements = firstLevelElements;
	    }

	    public MyElement[] getFirstLevelElements() {
	        return firstLevelElements;
	    }
	    
		public static MyInput init() {
			MyElement[] firstLevelElements = genElement(10);
			for (MyElement myElement : firstLevelElements) {
				myElement.setChildren(genElement(5));
			}
		    return new MyInput(firstLevelElements);
		}

		private static MyElement[] genElement(int size) {
			MyElement[] firstLevelElements = new MyElement[size] ;
		    for (int i = 0; i < size; i++) {
		    	firstLevelElements[i] = new MyElement();
		    	firstLevelElements[i].setName("name"+i);
			}
			return firstLevelElements;
		}
}
