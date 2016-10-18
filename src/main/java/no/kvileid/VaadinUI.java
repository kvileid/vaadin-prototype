package no.kvileid;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Max;

import org.hibernate.validator.constraints.NotBlank;

import com.google.common.collect.Lists;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.UI;

@Theme("valo")
@SpringUI(path = "")
public class VaadinUI extends UI {
	@Override
	protected void init(VaadinRequest request) {
		Grid grid = new Grid();
		BeanItemContainer<Person> container = new BeanItemContainer<>(Person.class, createPersons());
		grid.setContainerDataSource(container);
		grid.setEditorEnabled(true);
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setEditorFieldGroup(new BeanFieldGroup<Person>(Person.class));
		grid.getColumn("name").getEditorField().addValidator(new UniqueBeanValidator<Person>(grid) {
			@Override
			protected boolean isEqual(Object name, Person person) {
				return person.getName().equals(name);
			}

		});
		setContent(grid);
		setSizeFull();
	}

	private List<Person> createPersons() {
		return Lists.newArrayList(new Person("Truls", 45), new Person("Viktor", 6));
	}

	public abstract static class UniqueBeanValidator<T> implements Validator {
		private final Grid grid;
		private final BeanItemContainer<T> container;

		@SuppressWarnings("unchecked")
		public UniqueBeanValidator(Grid grid) {
			this.grid = grid;
			this.container = (BeanItemContainer<T>) grid.getContainerDataSource();
		}

		@Override
		public void validate(Object value) throws InvalidValueException {
			long count = getBeans().stream().filter(bean -> isEqual(value, bean)).count();
			if (count > 0) {
				throw new InvalidValueException("Har allerede brukt navn; " + value);
			}
		}

		protected abstract boolean isEqual(Object value, T bean);

		private List<T> getBeans() {
			T edited = (T) grid.getEditedItemId();
			return container.getItemIds().stream().filter(bean -> bean != edited).collect(Collectors.toList());
		}
	}

	public static class Person {
		@NotBlank
		private String name;
		@Max(value = 50)
		private int age;

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}

}
