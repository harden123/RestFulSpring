package restfulspring.view.tree;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyTreeInput {
	  private MyTreeElement[] firstLevelElements;

	    public MyTreeInput(MyTreeElement[] firstLevelElements) {
	        this.firstLevelElements = firstLevelElements;
	    }

	    public MyTreeElement[] getFirstLevelElements() {
	        return firstLevelElements;
	    }
	    
		public static MyTreeInput mockMyTreeInput(int size) {
			MyTreeElement[] firstLevelElements = mockFirstLevel(size);
		    return new MyTreeInput(firstLevelElements);
		}

		public static MyTreeElement[] mockFirstLevel(int size) {
			MyTreeElement[] firstLevelElements = initMockData(size);
			for (MyTreeElement myElement : firstLevelElements) {
				myElement.setChildren(initMockData(size/2));
			}
			return firstLevelElements;
		}

		private static MyTreeElement[] initMockData(int size) {
			MyTreeElement[] firstLevelElements = new MyTreeElement[size] ;
		    for (int i = 0; i < size; i++) {
		    	firstLevelElements[i] = new MyTreeElement();
		    	firstLevelElements[i].setName("name"+i);
			}
			return firstLevelElements;
		}
}
