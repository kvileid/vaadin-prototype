package no.kvileid;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.vaadin.annotations.Theme;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("valo")
@SpringUI(path = "")
public class VaadinUI extends UI {
	@Override
	protected void init(VaadinRequest request) {
		setContent(new HorizontalLayout(first(),second()));
        setSizeFull();
	}
	
	private Component first() {
		VerticalLayout l = new VerticalLayout();
		BeanFieldGroup<Person> fg = new BeanFieldGroup<>(Person.class);
		fg.setItemDataSource(new Person());
		fg.setBuffered(true);
		l.addComponent(fg.buildAndBind("Name", "name"));
		l.addComponent(fg.buildAndBind("Age", "age"));
		
		l.addComponent(new Button("Validate", e -> System.out.println(fg.isValid())));
		return l;
	}
	
	private Component second() {
		VerticalLayout layout = new VerticalLayout();
        Person bean = new Person();
        
        // Form for editing the bean
        final BeanFieldGroup<Person> form =
                new BeanFieldGroup<Person>(Person.class);
        form.setItemDataSource(bean);
        layout.addComponent(form.buildAndBind("Name", "name"));
        layout.addComponent(form.buildAndBind("Age", "age"));
        
        final Label error = new Label("", ContentMode.HTML);
        error.setVisible(false);
        layout.addComponent(error);

        // Buffer the form content
        form.setBuffered(true);
        layout.addComponent(new Button("OK", new ClickListener() {
            private static final long serialVersionUID = 8273374540088290859L;

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    form.commit();
                    Notification.show("OK!");
                    error.setVisible(false);
                } catch (CommitException e) {
                    for (Field<?> field: form.getFields()) {
                        ErrorMessage errMsg = ((AbstractField<?>)field).getErrorMessage();
                        if (errMsg != null) {
                            error.setValue("Error in " +
                                field.getCaption() + ": " +
                                errMsg.getFormattedHtmlMessage());
                            error.setVisible(true);
                            break;
                        }
                    }
                }
            }
        }));
        return layout;
	}
		
	public static class Person{
		private static final String bla = "blabla du må gjøre det og det";
		@NotNull(message = bla)
		@NotEmpty
		private String name;
		private int age;
		
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
