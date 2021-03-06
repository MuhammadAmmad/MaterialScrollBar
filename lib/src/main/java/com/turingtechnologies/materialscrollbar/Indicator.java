/*
 *  Copyright © 2016, Turing Technologies, an unincorporated organisation of Wynne Plaga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.turingtechnologies.materialscrollbar;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Devs should not normally need to extend this class. Just use {@link CustomIndicator} instead.
 * However, this is public to leave the option open.
 */
@SuppressWarnings("unchecked")
public abstract class Indicator<T> extends RelativeLayout{

    protected TextView textView;
    protected Context context;
    private boolean addSpace;
    private MaterialScrollBar materialScrollBar;
    private boolean rtl;
    private int size;

    public Indicator(Context context) {
        super(context);
        this.context = context;
        textView = new TextView(context);
        setVisibility(INVISIBLE);
    }

    void setSizeCustom(int size){
        if(addSpace){
           this.size =  size + Utils.getDP(10, this);
        } else {
            this.size =  size;
        }
        setLayoutParams(refreshMargins((LayoutParams) getLayoutParams()));
    }

    void setRTL(boolean rtl){
        this.rtl = rtl;
    }

    void linkToScrollBar(MaterialScrollBar msb, boolean addSpace){
        this.addSpace = addSpace;
        materialScrollBar = msb;

        if(addSpace){
            size = Utils.getDP(15, this)  + materialScrollBar.handleThumb.getWidth();
        } else {
            size = Utils.getDP(2, this)  + materialScrollBar.handleThumb.getWidth();
        }

        ViewCompat.setBackground(this, ContextCompat.getDrawable(context, rtl ? R.drawable.indicator_ltr : R.drawable.indicator));

        LayoutParams lp = new LayoutParams(Utils.getDP(getIndicatorWidth(), this), Utils.getDP(getIndicatorHeight(), this));
        lp = refreshMargins(lp);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getTextSize());
        LayoutParams tvlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        addView(textView, tvlp);

        ((GradientDrawable)getBackground()).setColor(msb.handleColour);

        if (rtl) {
            lp.addRule(ALIGN_LEFT, msb.getId());
        } else {
            lp.addRule(ALIGN_RIGHT, msb.getId());
        }
        ((ViewGroup)msb.getParent()).addView(this, lp);
    }

    LayoutParams refreshMargins(LayoutParams lp){
        if(rtl) {
            lp.setMargins(size, 0, 0, 0);
        } else {
            lp.setMargins(0, 0, size, 0);
        }
        return lp;
    }

    /**
     * Used by the materialScrollBar to move the indicator with the handleThumb
     * @param y Position to which the indicator should move.
     */
    void setScroll(float y){
        if(getVisibility() == VISIBLE){
            y -= 75 - materialScrollBar.getIndicatorOffset() + Utils.getDP(getIndicatorHeight() / 2, this);

            if(y < 5){
                y = 5;
            }

            ViewCompat.setY(this, y);
        }
    }

    /**
     * Sets the content text for the indicator and resizes if needed
     */
    void setText(int section){
        String newText;
        try{
            newText = getTextElement(section, (T) materialScrollBar.recyclerView.getAdapter());
        } catch (ArrayIndexOutOfBoundsException e){
            newText = "Error";
        }
        if (!textView.getText().equals(newText)){
            textView.setText(newText);

            LayoutWrapContentUpdater.wrapContentAgain(this);
        }
    }

    /**
     * This method should test the adapter to make sure that it implements the needed interface(s).
     *
     * @param adapter The adapter of the attached {@link RecyclerView}.
     */
    void testAdapter(RecyclerView.Adapter adapter){
        try{
            getTextElement(0, (T)adapter);
        } catch (ClassCastException e){
            throw new CustomExceptions.AdapterNotSetupForIndicatorException(adapter.getClass(), Utils.getGenericName(this));
        }
    }

    /**
     * Used by the materialScrollBar to change the text colour for the indicator.
     * @param colour The desired text colour.
     */
    void setTextColour(int colour){
        textView.setTextColor(colour);
    }

    /**
     * @param currentSection The section that the indicator is indicating for.
     * @param adapter The adapter of the attached {@link RecyclerView}.
     * @return The text that should go in the indicator.
     */
    protected abstract String getTextElement(Integer currentSection, T adapter);

    /**
     * @return The height of the indicator in px. If it is variable return any value and resize
     * the view yourself.
     */
    protected abstract int getIndicatorHeight();

    /**
     * @return The width of the indicator in px. If it is variable return any value and resize
     * the view yourself.
     */
    protected abstract int getIndicatorWidth();

    /**
     * @return The size of text in the indicator.
     */
    protected abstract int getTextSize();

}
