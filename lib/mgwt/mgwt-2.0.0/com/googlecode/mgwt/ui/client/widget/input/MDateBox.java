/*
 * Copyright 2014 Daniel Kurka
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.mgwt.ui.client.widget.input;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ValueBoxBase;

import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.widget.base.MValueBoxBase;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * A simple Date input widget. So far it uses &lt;input type="date" /> on iOS with a nice looking
 * native date picker.
 *
 * On other platforms it displays the pattern to use for date input (no picker so far).
 *
 * <h2>Date format</h2>
 *
 * For iOS there is no need to set a dateformat, since the iOS will automatically display the date
 * formated according to the locale of the device, while we get the date in the W3C Format.
 *
 * On other platforms you can set the format to fit what you like.
 *
 *
 *
 * @author Daniel Kurka
 *
 */
public class MDateBox extends MValueBoxBase<Date> {

  private static final DateTimeFormat DEFAULT_FORMAT = DateTimeFormat.getFormat("dd/MM/yy");
  private static final DateTimeFormat W3C_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

  /**
   * Using ValueBoxBase as a base class for our input element.
   *
   * @author Daniel Kurka
   *
   */
  private static class DateValueBoxBase extends ValueBoxBase<Date> implements HasSource {

    private final DateRenderer dateRenderer;
    private final DateParser dateParser;
    private Object source;

    protected DateValueBoxBase(DateRenderer dateRenderer, DateParser dateParser) {
      super(DOM.createInputText(), dateRenderer, dateParser);
      this.dateRenderer = dateRenderer;
      this.dateParser = dateParser;
    }

    public DateParser getDateParser() {
      return dateParser;
    }

    public DateRenderer getDateRenderer() {
      return dateRenderer;
    }

    @Override
    protected HandlerManager createHandlerManager() {
      return new HandlerManager(source);
    }

    public void setSource(Object source) {
      this.source = source;
    }

  }

  public static class DateRenderer implements Renderer<Date> {

    private DateTimeFormat format;

    public DateRenderer() {
      setFormat(DEFAULT_FORMAT);
    }

    public void setFormat(DateTimeFormat format) {
      this.format = format;
    }

    @Override
    public String render(Date object) {
      if (object == null) {
        return "";
      }
      return format.format(object);
    }

    @Override
    public void render(Date object, Appendable appendable) throws IOException {
      if (object != null) {
        appendable.append(format.format(object));
      }
    }
  }

  public static class DateParser implements Parser<Date> {

    public DateParser() {
      setFormat(DEFAULT_FORMAT);
    }

    private DateTimeFormat format;

    public void setFormat(DateTimeFormat format) {
      this.format = format;
    }

    @Override
    public Date parse(CharSequence text) throws ParseException {

      String string = text.toString();
      try {
        return format.parse(string);
      } catch (Exception e) {
        return null;
      }

    }

  }

  private Date lastValue;
  private DateTimeFormat format;

  public MDateBox() {
    this(InputApperanceHolder.DEFAULT_APPERAERANCE);
  }

  public MDateBox(InputAppearance appearance) {
    super(appearance, new DateValueBoxBase(new DateRenderer(), new DateParser()));
    format = DEFAULT_FORMAT;
    setPlaceHolder(DEFAULT_FORMAT.getPattern());

    addStyleName(appearance.css().textBox());

    // fix ios issue with onchange event

    if (MGWT.getOsDetection().isAndroid4_4_OrHigher()) {
      // only set input type to date if there is a native picker
      impl.setType(box.getElement(), "date");
      // use w3c format
      format = W3C_FORMAT;
    }

    if (MGWT.getOsDetection().isRetina()) {
      // IOS needs a workaround for empty date picker
      // Since it will not render them properly (iOS7)
      format = W3C_FORMAT;
      box.addFocusHandler(new FocusHandler() {

        @Override
        public void onFocus(FocusEvent event) {
          impl.setType(box.getElement(), "date");
        }
      });

      box.addBlurHandler(new BlurHandler() {

        @Override
        public void onBlur(BlurEvent event) {
          impl.setType(box.getElement(), "text");
        }
      });
    }

    if (MGWT.getOsDetection().isIPadRetina() || MGWT.getOsDetection().isIPad()) {
      // for iPad workaround does not work
      // adding default date, not happy about this
      impl.setType(box.getElement(), "date");
      format = W3C_FORMAT;

      Scheduler.get().scheduleDeferred(new ScheduledCommand() {

        @Override
        public void execute() {
           box.setValue(new Date());
        }
      });
    }


    // apply format to parsers
    getBox().getDateParser().setFormat(format);
    getBox().getDateRenderer().setFormat(format);

    if (MGWT.getOsDetection().isIOs()) {
      addBlurHandler(new BlurHandler() {

        @Override
        public void onBlur(BlurEvent event) {
          Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
              Date value = box.getValue();
              ValueChangeEvent.fireIfNotEqual(box, lastValue, value);
              lastValue = value;

            }
          });

        }
      });
      lastValue = null;
    }

  }

  /**
   * set the format to use in the datebox. Important: This should only be set on non iOS devices,
   * since iOS handles locale on dates under the cover.
   *
   * See: {@link MDateBox}
   *
   * @param pattern
   */
  public void setFormat(String pattern) {
    format = DateTimeFormat.getFormat(pattern);

    if (!MGWT.getOsDetection().isIOs() && !MGWT.getOsDetection().isAndroid4_4_OrHigher()) {
      setPlaceHolder(pattern);
    }

    getBox().getDateParser().setFormat(format);
    getBox().getDateRenderer().setFormat(format);

  }

  protected DateValueBoxBase getBox() {
    return (DateValueBoxBase) box;
  }

}
