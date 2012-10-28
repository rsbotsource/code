package org.powerbot.game.client;

import java.awt.Canvas;
import java.awt.Rectangle;

import org.powerbot.game.client.input.Keyboard;
import org.powerbot.game.client.input.Mouse;

public interface Client {
	public int getDestX();

	public int getCamPosX();

	public int getSubMenuY();

	public int getDestY();

	public int getMinimapSetting();

	public int isItemSelected();

	public Canvas getCanvas();

	public int getPlayerCount();

	public boolean[] getValidRSInterfaceArray();

	public boolean isMenuCollapsed();

	public int getMenuY();

	public int getCamPosZ();

	public int getCameraYaw();

	public boolean isMenuOpen();

	public float getMinimapAngle();

	public Mouse getMouse();

	public int getLoginIndex();

	public int[] getSkillLevelMaxes();

	public Object getRSGroundInfo();

	public String getCurrentPassword();

	public Object getCollapsedMenuItems();

	public int[] getSkillExperienceMaxes();

	public int getMinimapScale();

	public int[] getRSPlayerIndexArray();

	public int getMenuWidth();

	public int getSubMenuWidth();

	public int getRSPlayerCount();

	public int getMenuOptionsCountCollapsed();

	public Object getMyRSPlayer();

	public Rectangle[] getRSInterfaceBoundsArray();

	public Object getMenuItems();

	public String getCurrentUsername();

	public Object getRSItemHashTable();

	public Object getRSInterfaceNC();

	public int getRSNPCCount();

	public int getMenuX();

	public int getCameraPitch();

	public int getGUIRSInterfaceIndex();

	public Keyboard getKeyboard();

	public String getSelectedItemName();

	public int[] getSkillLevels();

	public Object getRSNPCNC();

	public int getPlane();

	public int getSubMenuX();

	public int getMenuHeight();

	public Callback getCallback();

	public Object getDetailInfoNode();

	public Object getCurrentMenuGroupNode();

	public Object getSettingArray();

	public int getCamPosY();

	public Object[] getRSPlayerArray();

	public boolean isSpellSelected();

	public int[] getRSNPCIndexArray();

	public int getLoopCycle();

	public int getMenuOptionsCount();

	public int[] getSkillExperiences();

	public Object getRSItemDefLoader();

	public int getMinimapOffset();

	public Object[] getRSInterfaceCache();

	public void setCallback(Callback callback);
}
