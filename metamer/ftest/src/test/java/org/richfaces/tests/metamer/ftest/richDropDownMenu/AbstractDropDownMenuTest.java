/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2016, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.tests.metamer.ftest.richDropDownMenu;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.richfaces.fragment.common.Event;
import org.richfaces.fragment.common.Locations;
import org.richfaces.fragment.common.Utils;
import org.richfaces.fragment.dropDownMenu.RichFacesDropDownMenu;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.extension.attributes.coverage.annotations.CoversAttributes;
import org.richfaces.tests.metamer.ftest.richContextMenu.ContextMenuSimplePage;
import org.richfaces.tests.metamer.ftest.webdriver.Attributes;

/**
 * Abstract test used for testing both drop down menus - top and side
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public abstract class AbstractDropDownMenuTest extends AbstractWebDriverTest {

    protected Integer delay;
    protected Integer[] delays = { 1000, 1500, 1900 };

    private final Attributes<DropDownMenuAttributes> dropDownMenuAttributes = getAttributes();

    @Page
    private DropDownMenuPage page;

    protected RichFacesDropDownMenu getCurrentMenu() {
        return page.getFileDropDownMenu(driver.getCurrentUrl());
    }

    public DropDownMenuPage getPage() {
        return page;
    }

    public String returnPopupWidth(String minWidth, RichFacesDropDownMenu dropDownMenu) {
        dropDownMenuAttributes.set(DropDownMenuAttributes.popupWidth, minWidth);
        dropDownMenu.advanced().show(page.getTarget1());
        return page.getDropDownMenuContent().getCssValue("min-width");
    }

    @CoversAttributes("dir")
    public void testDir() {
        updateDropDownMenuInvoker();
        String expected = "rtl";
        dropDownMenuAttributes.set(DropDownMenuAttributes.dir, expected);

        getCurrentMenu().advanced().show(page.getTarget1());
        String directionCSS = getCurrentMenu().advanced().getItemsElements().get(0)
            .getCssValue("direction");
        assertEquals(directionCSS, expected, "The direction attribute was not applied correctly!");
    }

    @CoversAttributes("direction")
    public void testDirection() {
        updateDropDownMenuInvoker();
        testDirection(new ShowElementAndReturnAction() {
            @Override
            public WebElement perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                return getCurrentMenu().advanced().getMenuPopup();
            }
        });
    }

    @CoversAttributes("disabled")
    public void testDisabled() {
        updateDropDownMenuInvoker();
        getCurrentMenu().advanced().show(page.getTarget1());

        dropDownMenuAttributes.set(DropDownMenuAttributes.disabled, true);

        try {
            getCurrentMenu().advanced().show(page.getTarget1());
            fail("The context menu should not be invoked when disabled!");
        } catch (TimeoutException ex) {
            // OK
        }
    }

    @CoversAttributes("hideDelay")
    public void testHideDelay(int delay) {
        WebElement menuRoot = driver.findElement(By.cssSelector("[id$='menu1']"));
        new MenuDelayTester().testHideDelay(menuRoot, delay, new Event[] { Event.MOUSEOVER, Event.MOUSEOUT }, menuRoot);
    }

    @CoversAttributes("horizontalOffset")
    public void testHorizontalOffset() {
        updateDropDownMenuInvoker();
        testHorizontalOffset(new ShowElementAndReturnAction() {
            @Override
            public WebElement perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                return getCurrentMenu().advanced().getMenuPopup();
            }
        });
    }

    public void testInit() {
        page.fullPageRefresh();
        updateDropDownMenuInvokerToClick();
        getAttributes().set(DropDownMenuAttributes.hideDelay, 3000);
        assertPresent(page.getFileMenu(), "Drop down menu \"File\" should be present on the page.");
        assertVisible(page.getFileMenu(), "Drop down menu \"File\" should be visible on the page.");

        assertPresent(page.getGroup(), "Menu group \"Save As...\" should be present on the page.");
        assertNotVisible(page.getGroup(), "Menu group \"Save As...\" should not be visible on the page.");

        assertNotVisible(page.getFileMenuList(), "Menu should not be expanded.");
        page.getTarget1().click();
        assertVisible(page.getFileMenuList(), "Menu should be expanded.");

        assertPresent(page.getGroup(), "Menu group \"Save As...\" should be present on the page.");
        assertVisible(page.getGroup(), "Menu group \"Save As...\" should be visible on the page.");

        assertPresent(page.getMenuItem41(), "Menu item \"Save\" should be present on the page.");
        assertNotVisible(page.getMenuItem41(), "Menu item \"Save\" should not be visible on the page.");
        assertNotVisible(page.getGroupList(), "Submenu should not be expanded.");

        guardNoRequest(new Actions(driver).click(getCurrentMenu().advanced().getItemsElements().get(3)).build())
            .perform();

        assertVisible(page.getGroupList(), "Submenu should be expanded.");

        assertPresent(page.getMenuItem41(), "Menu item \"Save\" should be present on the page.");
        assertVisible(page.getMenuItem41(), "Menu item \"Save\" should be visible on the page.");

        assertPresent(page.getIcon(), "Icon of menu group should not be present on the page.");

        assertPresent(page.getFileMenuLabelOriginal(), "Label of menu should be present on the page.");
        assertVisible(page.getFileMenuLabelOriginal(), "Label of menu should be visible on the page.");

        assertEquals(page.getFileMenuLabelOriginal().getText(), "File", "Label of the menu");
    }

    @CoversAttributes("jointPoint")
    public void testJointPoint() {
        updateDropDownMenuInvoker();
        getCurrentMenu().advanced().show(page.getTarget1());
        Locations l = Utils.getLocations(page.getFileMenu());
        testJointPoint(l.getWidth(), l.getHeight(), new ShowElementAndReturnAction() {
            @Override
            public WebElement perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                return getCurrentMenu().advanced().getMenuPopup();
            }
        });
    }

    @CoversAttributes("lang")
    public void testLang() {
        updateDropDownMenuInvoker();
        String langVal = "cs";
        dropDownMenuAttributes.set(DropDownMenuAttributes.lang, langVal);
        getCurrentMenu().advanced().show(page.getTarget1());

        assertEquals(getCurrentMenu().advanced().getLangAttribute(), langVal,
            "The lang attribute was not set correctly!");
    }

    @CoversAttributes("mode")
    public void testMode() {
        updateDropDownMenuInvokerToClick();
        // ajax
        dropDownMenuAttributes.set(DropDownMenuAttributes.mode, "ajax");
        getAttributes().set(DropDownMenuAttributes.hideDelay, 2000);
        page.getTarget1().click();
        guardAjax(new Actions(driver).click(getCurrentMenu().advanced().getItemsElements().get(0)).build())
            .perform();
        assertEquals(page.getOutput().getText(), "New", "Menu action was not performed.");

        // server
        dropDownMenuAttributes.set(DropDownMenuAttributes.mode, "server");
        page.getTarget1().click();
        guardHttp(new Actions(driver).click(getCurrentMenu().advanced().getItemsElements().get(1)).build())
            .perform();
        assertEquals(page.getOutput().getText(), "Open", "Menu action was not performed.");

        // client
        dropDownMenuAttributes.set(DropDownMenuAttributes.mode, "client");
        getCurrentMenu().advanced().show(page.getTarget1());
        guardNoRequest(getCurrentMenu().advanced().getItemsElements().get(0)).click();

        // null
        dropDownMenuAttributes.set(DropDownMenuAttributes.mode, "server");
        getCurrentMenu().advanced().show(page.getTarget1());
        guardHttp(getCurrentMenu().advanced().getItemsElements().get(1)).click();
        assertEquals(page.getOutput().getText(), "Open", "Menu action was not performed.");
    }

    public void testNoResourceErrorPresent() {
        checkNoResourceErrorPresent(new Action() {

            @Override
            public void perform() {
                updateDropDownMenuInvoker();
                getCurrentMenu().advanced().show(page.getTarget1());
            }
        });
    }

    @CoversAttributes("onclick")
    public void testOnclick() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onclick, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                getCurrentMenu().advanced().getItemsElements().get(1).click();
            }
        });
    }

    @CoversAttributes("ondblclick")
    public void testOndblclick() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.ondblclick, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                new Actions(driver)
                    .doubleClick(getCurrentMenu().advanced().getItemsElements().get(2))
                    .build().perform();
            }
        });
    }

    @CoversAttributes("ongrouphide")
    public void testOngrouphide() {
        updateDropDownMenuInvokerToClick();
        getAttributes().set(DropDownMenuAttributes.hideDelay, 3000);
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.ongrouphide, new Action() {
            @Override
            public void perform() {
                page.getTarget1().click();
                new Actions(driver)
                    .click(
                        getCurrentMenu().advanced().getItemsElements().get(3)).build()
                    .perform();
                waitGui().until().element(page.getGroupList()).is().visible();
                new Actions(driver)
                    .click(getCurrentMenu().advanced().getItemsElements().get(1))
                    .build().perform();
                waitGui().until().element(page.getGroupList()).is().not().visible();
            }
        });
    }

    @CoversAttributes("ongroupshow")
    public void testOngroupshow() {
        updateDropDownMenuInvokerToClick();
        getAttributes().set(DropDownMenuAttributes.hideDelay, 500);
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.ongroupshow, new Action() {
            @Override
            public void perform() {
                page.getTarget1().click();
                new Actions(driver)
                    .click(
                        getCurrentMenu().advanced().getItemsElements().get(3)).build()
                    .perform();
                waitGui().until().element(page.getGroupList()).is().visible();
            }
        });
    }

    @CoversAttributes("onhide")
    public void testOnhide() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onhide, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                getCurrentMenu().advanced().hide();
            }
        });
    }

    @CoversAttributes("onitemclick")
    public void testOnitemclick() {
        updateDropDownMenuInvoker();
        testOnclick();
    }

    @CoversAttributes("onkeydown")
    public void testOnkeydown() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onkeydown, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                new Actions(driver)
                    .keyDown(getCurrentMenu().advanced().getItemsElements().get(2),
                        Keys.CONTROL)
                    .keyUp(getCurrentMenu().advanced().getItemsElements().get(2),
                        Keys.CONTROL).build().perform();
            }
        });
    }

    @CoversAttributes("onkeypress")
    public void testOnkeypress() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onkeypress, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                new Actions(driver).sendKeys("a").build().perform();
            }
        });
    }

    @CoversAttributes("onkeyup")
    public void testOnkeyup() {
        testFireEventWithJS(getCurrentMenu().advanced().getMenuPopup(),
            dropDownMenuAttributes, DropDownMenuAttributes.onkeyup);
    }

    @CoversAttributes("onmousedown")
    public void testOnmousedown() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onmousedown, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                Mouse mouse = ((HasInputDevices) driver).getMouse();
                mouse.mouseDown(((Locatable) getCurrentMenu().advanced()
                    .getItemsElements().get(1)).getCoordinates());
            }
        });
    }

    @CoversAttributes("onmousemove")
    public void testOnmousemove() {
        testFireEventWithJS(getCurrentMenu().advanced().getMenuPopup(),
            dropDownMenuAttributes, DropDownMenuAttributes.onmousemove);
    }

    @CoversAttributes("onmouseout")
    public void testOnmouseout() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onmouseout, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                new Actions(driver)
                    .moveToElement(
                        getCurrentMenu().advanced().getItemsElements().get(3)).build()
                    .perform();
                waitModel().until().element(page.getGroupList()).is().visible();
                new Actions(driver).moveToElement(page.getRequestTimeElement()).build().perform();
                waitGui().until().element(page.getGroupList()).is().not().visible();
            }
        });
    }

    @CoversAttributes("onmouseover")
    public void testOnmouseover() {
        testFireEventWithJS(getCurrentMenu().advanced().getMenuPopup(),
            dropDownMenuAttributes, DropDownMenuAttributes.onmouseover);
    }

    @CoversAttributes("onmouseup")
    public void testOnmouseup() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onmouseup, new Action() {
            @Override
            public void perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                Mouse mouse = ((HasInputDevices) driver).getMouse();
                Coordinates coords = ((Locatable) getCurrentMenu().advanced()
                    .getItemsElements().get(1)).getCoordinates();
                mouse.mouseDown(coords);
                mouse.mouseUp(coords);
            }
        });
    }

    @CoversAttributes("onshow")
    public void testOnshow() {
        updateDropDownMenuInvoker();
        testFireEvent(dropDownMenuAttributes, DropDownMenuAttributes.onshow, new Action() {
            @Override
            public void perform() {
                new Actions(driver).moveToElement(page.getTarget1()).perform();
//                page.getFileDropDownMenu(driver.getCurrentUrl()).advanced().invoke(page.getTarget1());
            }
        });
        new Actions(driver).moveToElement(page.getRequestTimeElement()).perform();
    }

    @CoversAttributes("popupWidth")
    public void testPopupWidth() {
        updateDropDownMenuInvoker();
        String minWidth = "333";
        assertEquals(returnPopupWidth(minWidth, getCurrentMenu()), minWidth + "px");

        minWidth = "250";
        assertEquals(returnPopupWidth(minWidth, getCurrentMenu()), minWidth + "px");

        minWidth = "250";
        assertEquals(returnPopupWidth(minWidth, getCurrentMenu()), minWidth + "px");
    }

    @CoversAttributes("rendered")
    public void testRendered() {
        updateDropDownMenuInvoker();
        dropDownMenuAttributes.set(DropDownMenuAttributes.rendered, Boolean.FALSE);
        assertNotPresent(page.getFileMenuLabel(), "The drop down menu for file can not be rendered!");
    }

    @CoversAttributes("showDelay")
    public void testShowDelay(int delay) {
        WebElement menuRoot = driver.findElement(By.cssSelector("[id$='menu1']"));
        new MenuDelayTester().testShowDelay(menuRoot, delay, Event.MOUSEOVER, menuRoot.findElement(By.tagName("div")));
    }

    @CoversAttributes("showEvent")
    public void testShowEvent() {
        dropDownMenuAttributes.set(DropDownMenuAttributes.showEvent, "contextmenu");
        getCurrentMenu().advanced().setShowEvent(Event.CONTEXTCLICK);
        getCurrentMenu().advanced().show(page.getTarget1());

        dropDownMenuAttributes.set(DropDownMenuAttributes.showEvent, "mouseover");
        getCurrentMenu().advanced().setShowEvent(Event.MOUSEOVER);
        getCurrentMenu().advanced().show(page.getTarget1());

        dropDownMenuAttributes.set(DropDownMenuAttributes.showEvent, "click");
        getCurrentMenu().advanced().setShowEvent(Event.CLICK);
        getCurrentMenu().advanced().show(page.getTarget1());
    }

    @CoversAttributes("style")
    public void testStyle() {
        updateDropDownMenuInvoker();
        String color = "yellow";
        String styleVal = "background-color: " + color + ";";
        dropDownMenuAttributes.set(DropDownMenuAttributes.style, styleVal);
        getCurrentMenu().advanced().show(page.getTarget1());
        String backgroundColor = getCurrentMenu().advanced().getTopLevelElement()
            .getCssValue("background-color");
        // webdriver retrieves the color in rgba format
        assertEquals(ContextMenuSimplePage.trimTheRGBAColor(backgroundColor), "rgba(255,255,0,1)",
            "The style was not applied correctly!");
    }

    @CoversAttributes("styleClass")
    public void testStyleClass() {
        testStyleClass(getCurrentMenu().advanced().getTopLevelElement());
    }

    @CoversAttributes("styleClass")
    public void testStyleClassWhenDisabled() {
        setAttribute("disabled", true);
        testStyleClass();
    }

    public void testSubMenuOpening() {
        updateDropDownMenuInvokerToClick();
        jsUtils.scrollToView(getCurrentMenu().advanced().getTopLevelElement());
        Graphene.guardAjax(getCurrentMenu().expandGroup("Save As...", page.getTarget1())).selectItem("Save All");
        assertEquals(page.getOutput().getText(), "Save All");
    }

    @CoversAttributes("title")
    public void testTitle() {
        updateDropDownMenuInvoker();
        String titleVal = "test title";
        dropDownMenuAttributes.set(DropDownMenuAttributes.title, titleVal);
        assertEquals(
            getCurrentMenu().advanced().getTopLevelElement().getAttribute("title"),
            titleVal);
    }

    @CoversAttributes("verticalOffset")
    public void testVerticalOffset() {
        updateDropDownMenuInvoker();
        testVerticalOffset(new ShowElementAndReturnAction() {
            @Override
            public WebElement perform() {
                getCurrentMenu().advanced().show(page.getTarget1());
                return getCurrentMenu().advanced().getMenuPopup();
            }
        });
    }

    private void updateDropDownMenuInvoker() {
        getCurrentMenu().advanced().setShowEvent(Event.MOUSEOVER);
    }

    private void updateDropDownMenuInvokerToClick() {
        getCurrentMenu().advanced().setShowEvent(Event.CLICK);
    }
}
