package com.artifex.mupdfdemo;

import android.graphics.PointF;
import android.graphics.RectF;

enum Hit {Nothing, Widget, Annotation}

public interface MuPDFView {
	void setPage(int page, PointF size);
	void setScale(float scale);
	int getPage();
	void blank(int page);
	Hit passClickEvent(float x, float y);
	LinkInfo hitLink(float x, float y);
	void selectText(float x0, float y0, float x1, float y1);
	void deselectText();
	boolean copySelection();
	boolean markupSelection(Annotation.Type type);
	void deleteSelectedAnnotation();
	void setSearchBoxes(RectF searchBoxes[]);
	void setLinkHighlighting(boolean f);
	void deselectAnnotation();
	void startDraw(float x, float y);
	void continueDraw(float x, float y);
	void cancelDraw();
	boolean saveDraw();
	void setChangeReporter(Runnable reporter);
	void update();
	void updateHq(boolean update);
	void removeHq();
	void releaseResources();
	void releaseBitmaps();
}
