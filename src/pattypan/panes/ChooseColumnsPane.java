/*
 * The MIT License
 *
 * Copyright 2016 Pawel Marynowski.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pattypan.panes;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pattypan.Session;
import pattypan.Settings;
import pattypan.Template;
import pattypan.TemplateField;
import pattypan.Util;
import pattypan.elements.WikiLabel;
import pattypan.elements.WikiPane;
import pattypan.elements.WikiScrollPane;

public class ChooseColumnsPane extends WikiPane {

  Stage stage;

  VBox templatePane = new VBox(10);
  VBox templateDescContainer = new VBox(4);
  VBox rightContainer = new VBox(4);
  Hyperlink wikicodeLink;

  VBox wikicodePane = new VBox(10);
  TextArea wikicodeText = new TextArea("");

  public ChooseColumnsPane(Stage stage) {
    super(stage, 0.5);
    this.stage = stage;

    setContent();
    setActions();
  }

  /*
   * set content and actions
   *****************************************************************************
   */
  public WikiPane getContent() {
    return this;
  }

  private WikiPane setActions() {
    wikicodeLink.setOnAction(event -> {
      templateDescContainer.getChildren().clear();
      templateDescContainer.getChildren().add(wikicodePane);
      Session.METHOD = "wikicode";
    });

    prevButton.linkTo("ChooseDirectoryPane", stage);
    nextButton.setOnAction(event -> {
      if (Session.METHOD.equals("wikicode")) {
        String text = wikicodeText.getText().trim();
        Session.VARIABLES = Template.getComputedVariablesFromString(text);
        Session.WIKICODE = text;
      }

      if (Session.METHOD.equals("template")) {
        Template template = Settings.TEMPLATES.get(Session.TEMPLATE);
        Session.VARIABLES = template.getComputedVariables();
        Session.WIKICODE = template.getComputedWikicode();
      }

      nextButton.goTo("CreateFilePane", stage);
    });
    showTemplateFieldsChoose(Session.TEMPLATE);
    return this;
  }

  private WikiPane setContent() {
    addElement("choose-columns-intro", 40);

    /* templates */
    rightContainer.getChildren().add(new WikiLabel("choose-columns-template").setClass("bold"));
    Settings.TEMPLATES.forEach((key, value) -> {
      Hyperlink label = new Hyperlink(key);
      label.setOnAction(event -> {
        Session.METHOD = "template";
        Session.TEMPLATE = key;
        showTemplateFieldsChoose(Session.TEMPLATE);
      });
      rightContainer.getChildren().add(label);
    });

    /* advanced */
    wikicodeLink = new Hyperlink(Util.text("choose-columns-wikicode"));
    rightContainer.getChildren().addAll(
            new Region(),
            new WikiLabel("Advanced").setClass("bold"),
            wikicodeLink
    );

    addElementRow(templatePane, 10,
            new Node[]{new WikiScrollPane(rightContainer).setWidth(150), new WikiScrollPane(templateDescContainer)},
            new Priority[]{Priority.NEVER, Priority.ALWAYS}
    );
    addElement(templatePane);

    /* wiki code pane */
    ComboBox templateBox = new ComboBox();
    templateBox.getItems().addAll(Settings.TEMPLATES.keySet().toArray());
    templateBox.setOnAction((Event ev) -> {
      String templateName = templateBox.getSelectionModel().getSelectedItem().toString();
      Template t = Settings.TEMPLATES.get(templateName);
      wikicodeText.setText(t.wikicode);
    });

    wikicodeText.getStyleClass().add("mw-ui-input");
    wikicodeText.setText(Session.WIKICODE);
    wikicodePane.getChildren().addAll(templateBox, wikicodeText);
    return this;
  }

  /*
   * methods
   *****************************************************************************
   */
  /**
   * Adds checkboxes with wikitemplate fields.
   *
   * @param templateName name of wikitemplate
   * @return true, if template exists
   */
  private boolean showTemplateFieldsChoose(String templateName) {
    Template template = Settings.TEMPLATES.get(templateName);

    Hyperlink docLink = new Hyperlink(Util.text("choose-columns-template-doc"));
    docLink.setMinHeight(25);
    docLink.setOnAction(event -> {
      Util.openUrl("https://commons.wikimedia.org/wiki/Template:" + template.name + "/doc");
    });

    templateDescContainer.getChildren().clear();
    templateDescContainer.getChildren().add(new HBox(10,
            new WikiLabel("{{" + template.name + "}}").setClass("header").setAlign("left"),
            docLink
    ));
    templateDescContainer.getChildren().add(new WikiLabel("choose-columns-template-intro").setAlign("left").setHeight(70));

    HBox headersContainer = new HBox(10);
    headersContainer.getChildren().addAll(
            new WikiLabel("choose-columns-fields-name").setClass("bold").setWidth(200, 495).setHeight(35),
            new WikiLabel("choose-columns-radio-buttons").setClass("bold").setWidth(115).setHeight(35),
            new WikiLabel("choose-columns-value").setClass("bold").setWidth(50, 500).setHeight(35));
    templateDescContainer.getChildren().add(headersContainer);

    for (TemplateField tf : template.variables) {
      templateDescContainer.getChildren().add(tf.getRow());
    }
    return true;
  }
}
