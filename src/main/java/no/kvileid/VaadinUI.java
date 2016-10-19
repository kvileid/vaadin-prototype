package no.kvileid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Max;

import org.hibernate.validator.constraints.NotBlank;

import com.google.common.collect.Lists;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.CommitErrorEvent;
import com.vaadin.ui.Grid.EditorErrorHandler;
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

		grid.setEditorErrorHandler(new MyEditorErrorHandler(grid));

		grid.getColumn("age").setHeaderCaption("Alder");
		grid.getColumn("name").setHeaderCaption("Navn").getEditorField()
				.addValidator(new UniqueBeanValidator<Person>() {
					@Override
					protected boolean isAlreadyUsed(Object name, Person person) {
						return person.getName().equals(name);
					}

					@Override
					protected Collection<Person> getAllBeans() {
						return container.getItemIds();
					}

					@Override
					protected Person getEditedBean() {
						return (Person) grid.getEditedItemId();
					}
				});
		setContent(grid);
		setSizeFull();
	}

	private List<Person> createPersons() {
		return Lists.newArrayList(new Person("Truls", 45), new Person("Viktor", 6));
	}

	public abstract static class UniqueBeanValidator<T> implements Validator {
		@Override
		public void validate(Object value) throws InvalidValueException {
			T editedBean = getEditedBean();
			long count = getAllBeans().stream().filter(bean -> bean != editedBean).filter(bean -> isAlreadyUsed(value, bean)).count();
			if (count > 0) {
				throw new InvalidValueException("Har allerede brukt " + value);
			}
		}

		protected abstract boolean isAlreadyUsed(Object value, T bean);
		protected abstract Collection<T> getAllBeans();
		protected abstract T getEditedBean();
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

	// Vaadin bug https://dev.vaadin.com/ticket/16806
	public class MyEditorErrorHandler implements EditorErrorHandler {
		private final Grid grid;

		public MyEditorErrorHandler(Grid grid) {
			this.grid = grid;
		}

		@Override
		public void commitError(CommitErrorEvent event) {
			Map<Field<?>, InvalidValueException> invalidFields = event.getCause().getInvalidFields();

			if (!invalidFields.isEmpty()) {
				Object firstErrorPropertyId = null;
				Field<?> firstErrorField = null;

				FieldGroup fieldGroup = event.getCause().getFieldGroup();
				for (Column column : grid.getColumns()) {
					Object propertyId = column.getPropertyId();
					Field<?> field = fieldGroup.getField(propertyId);
					if (invalidFields.keySet().contains(field)) {
						event.addErrorColumn(column);

						if (firstErrorPropertyId == null) {
							firstErrorPropertyId = propertyId;
							firstErrorField = field;
						}
					}
				}

				/*
				 * Validation error, show first failure as
				 * "<Column header>: <message>"
				 */
				String caption = grid.getColumn(firstErrorPropertyId).getHeaderCaption();

				InvalidValueException v = invalidFields.get(firstErrorField);
				event.setUserErrorMessage(caption + ": " + getRootCause(v).getLocalizedMessage());
			} else {
				com.vaadin.server.ErrorEvent.findErrorHandler(grid)
						.error(new ConnectorErrorEvent(grid, event.getCause()));
			}
		}

		private Throwable getRootCause(InvalidValueException e) {
			if (e.getCauses() != null && e.getCauses().length > 0) {
				return e.getCauses()[0];
			} else {
				return e;
			}
		}
	}
}
