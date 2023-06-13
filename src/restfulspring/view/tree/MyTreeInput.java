package restfulspring.view.tree;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyTreeInput {
	private List<MyTreeElement> firstLevelElements;

	public MyTreeInput(List<MyTreeElement> firstLevelElements) {
		this.firstLevelElements = firstLevelElements;
	}

	public List<MyTreeElement> getFirstLevelElements() {
		return firstLevelElements;
	}

	public static MyTreeInput mockMyTreeInput(int size) {
		List<MyTreeElement> firstLevelElements = mockFirstLevel(size);
		return new MyTreeInput(firstLevelElements);
	}

	public static List<MyTreeElement> mockFirstLevel(int size) {
		List<MyTreeElement> firstLevelElements = initMockData(size);
		for (MyTreeElement myElement : firstLevelElements) {
			myElement.setChildren(initMockData(size / 2));
		}
		return firstLevelElements;
	}

	private static List<MyTreeElement> initMockData(int size) {
		List<MyTreeElement> firstLevelElements = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			MyTreeElement f = new MyTreeElement();
			f.setName("name" + i);
			firstLevelElements.add(f);
		}
		return firstLevelElements;
	}
}
