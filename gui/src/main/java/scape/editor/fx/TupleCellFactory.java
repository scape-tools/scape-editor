package scape.editor.fx;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.imgscalr.Scalr;
import scape.editor.fs.graphics.RSFont;
import scape.editor.fs.graphics.RSSprite;
import scape.editor.fx.component.NumericTextField;
import scape.editor.gui.model.KeyModel;
import scape.editor.gui.model.NamedValueModel;
import scape.editor.gui.model.ValueModel;
import scape.editor.gui.util.BufferedImageExtensionsKt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TupleCellFactory extends TableCell<NamedValueModel, ValueModel> {

    private void updateMap(ValueModel model, Object value) {
        KeyModel keyModel = model.getModel();

        try {
            keyModel.getMap().put(model.getKey(), value);
            System.out.println(String.format("Updating key=%s value=%s", model.getKey(), value == null ? "null" : value.toString()));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void updateItem(ValueModel parent, boolean empty) {
        super.updateItem(parent, empty);

        if (parent != null) {
            ValueModel valueModel = (ValueModel) parent.getValue();
            Object item = valueModel.getValue();

            if (item != null) {
                if (item instanceof String) {
                    String string = (String)item;
                    setText(null);
                    TextField tf = new TextField(string);
                    tf.setAlignment(Pos.CENTER);
                    tf.textProperty().addListener(((observable, oldValue, newValue) -> updateMap(valueModel, newValue)));
                    setGraphic(tf);
                } else if (item instanceof Integer) {
                    int value = (Integer)item;
                    setText(null);

                    NumericTextField nTf = new NumericTextField();
                    nTf.setAlignment(Pos.CENTER);
                    nTf.setText(Integer.toString(value));
                    nTf.textProperty().addListener(((observable, oldValue, newValue) -> {
                        try {
                            if (!newValue.isEmpty()) {
                                int intValue = Integer.parseInt(newValue);
                                updateMap(valueModel, intValue);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }));

                    setGraphic(nTf);
                } else if (item instanceof Byte) {
                    byte value = (Byte) item;
                    NumericTextField nTf = new NumericTextField();
                    nTf.setAlignment(Pos.CENTER);
                    nTf.setText(Integer.toString(value));
                    nTf.textProperty().addListener(((observable, oldValue, newValue) -> {
                        try {
                            if (!newValue.isEmpty()) {
                                byte byteValue = Byte.parseByte(newValue);
                                updateMap(valueModel, byteValue);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }));
                    setText(null);
                    setGraphic(nTf);
                } else if (item instanceof Long) {
                    long value = (Long)item;
                    setText(null);

                    NumericTextField nTf = new NumericTextField();
                    nTf.setAlignment(Pos.CENTER);
                    nTf.setText(Long.toString(value));
                    nTf.textProperty().addListener(((observable, oldValue, newValue) -> {
                        try {
                            if (!newValue.isEmpty()) {
                                long longValue = Long.parseLong(newValue);
                                updateMap(valueModel, longValue);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }));
                    setGraphic(nTf);
                } else if (item instanceof Boolean) {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected((boolean) item);
                    checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                        boolean boolValue = newValue.booleanValue();
                        updateMap(valueModel, boolValue);
                    }));
                    setText(null);
                    setGraphic(checkBox);
                } else if (item instanceof String[]) {
                    ObservableList<String> items = FXCollections.observableArrayList((String[]) item);
                    ComboBox<String> comboBox = new ComboBox<>(items);
                    comboBox.setEditable(true);

                    comboBox.getEditor().textProperty().addListener(((observable, oldValue, newValue) -> {
                        final int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
                        KeyModel keyModel = valueModel.getModel();
                        String key = valueModel.getKey();
                        Object value = keyModel.getMap().get(key);

                        try {
                            if (value != null && value instanceof String[]) {
                                String[] array = (String[]) value;
                                if (selectedIndex >= 0 && selectedIndex < array.length) {
                                    array[selectedIndex] = newValue;
                                    items.set(selectedIndex, newValue);
                                    keyModel.getMap().put(key, array);
                                    System.out.println("Updated: " + Arrays.toString(array));
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }));

                    setText(null);
                    setGraphic(comboBox);
                } else if (item.getClass() == int[].class) {
                    ObservableList<Integer> items = FXCollections.observableArrayList(Ints.asList((int[]) item));
                    ComboBox<Integer> comboBox = new ComboBox(items);
                    comboBox.setEditable(true);

                    comboBox.getEditor().textProperty().addListener(((observable, oldValue, newValue) -> {
                        final int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
                        KeyModel keyModel = valueModel.getModel();
                        String key = valueModel.getKey();
                        Object value = keyModel.getMap().get(key);

                        try {
                            if (value != null && value.getClass() == int[].class) {
                                int[] array = (int[]) value;
                                int intValue = Integer.parseInt(newValue);

                                if (selectedIndex >= 0 && selectedIndex < array.length) {
                                    array[selectedIndex] = intValue;
                                    items.set(selectedIndex, intValue);
                                    keyModel.getMap().put(key, array);
                                    System.out.println("Updated: " + Arrays.toString(array));
                                }

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }));

                    setText(null);
                    setGraphic(comboBox);
                } else if (item.getClass() == short[].class) {
                    ObservableList<Short> items = FXCollections.observableArrayList(Shorts.asList((short[]) item));
                    ComboBox<Short> comboBox = new ComboBox(items);
                    comboBox.setEditable(true);

                    comboBox.getEditor().textProperty().addListener(((observable, oldValue, newValue) -> {
                        final int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
                        KeyModel keyModel = valueModel.getModel();
                        String key = valueModel.getKey();
                        Object value = keyModel.getMap().get(key);

                        try {
                            if (value != null && value.getClass() == short[].class) {
                                short[] array = (short[]) value;
                                short shortValue = Short.parseShort(newValue);

                                if (selectedIndex >= 0 && selectedIndex < array.length) {
                                    array[selectedIndex] = shortValue;
                                    items.set(selectedIndex, shortValue);
                                    keyModel.getMap().put(key, array);
                                    System.out.println("Updated: " + Arrays.toString(array));
                                }

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }));

                    setText(null);
                    setGraphic(comboBox);
                } else if (item instanceof RSSprite) {
                    RSSprite sprite = (RSSprite) item;
                    BufferedImage bimage = sprite.toBufferedImage();
                    ImageView imageView = new ImageView();
                    bimage = BufferedImageExtensionsKt.setColorTransparent(bimage, Color.BLACK);

                    if (bimage.getWidth() > 64 || bimage.getHeight() > 64) {
                        imageView.setImage(SwingFXUtils.toFXImage(Scalr.resize(bimage, 64), null));
                    } else {
                        imageView.setImage(SwingFXUtils.toFXImage(bimage, null));
                    }

                    setText(null);
                    setGraphic(imageView);
                } else if (item instanceof RSFont) {
                    RSFont font = (RSFont) item;
                    BufferedImage bimage = font.toBufferedImage();
                    ImageView imageView = new ImageView();
                    bimage = BufferedImageExtensionsKt.setColorTransparent(bimage, Color.BLACK);

                    if (bimage.getWidth() > 64 || bimage.getHeight() > 64) {
                        imageView.setImage(SwingFXUtils.toFXImage(Scalr.resize(bimage, 64), null));
                    } else {
                        imageView.setImage(SwingFXUtils.toFXImage(bimage, null));
                    }

                    setText(null);
                    setGraphic(imageView);
                } else {
                    setText(item.getClass().getTypeName());
                    setGraphic(null);
                }
            } else {
                setText(null);
                setGraphic(null);
            }
        } else {
            setText(null);
            setGraphic(null);
        }
    }

}
