package phy.jsf.util;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import phy.jsf.R;
import x.datautil.L;

import static android.widget.LinearLayout.SHOW_DIVIDER_BEGINNING;
import static android.widget.LinearLayout.SHOW_DIVIDER_END;
import static android.widget.LinearLayout.SHOW_DIVIDER_MIDDLE;
import static phy.network.HttpSender.CONTENT_JSONNAME;
import static phy.network.HttpSender.CONTENT_SELECTIONS;
import static phy.network.HttpSender.CONTENT_TITLE;
import static phy.network.HttpSender.CONTENT_TYPE;
import static phy.network.HttpSender.CONTENT_VAL;

public class ViewLoader {
    public static String typeText="text";
    public static String typeTextArea="textArea";
    public static String typeRadio="radio";
    public static String typeCheckbox="checkbox";
    public static String typeSelect="select";
    public static String typeTitle="title";


    public static LinearLayout loadChildLayout(Context context,LinearLayout layout_content,boolean multipeLines){
        LinearLayout child=new LinearLayout(context);
        child.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams params;
        if(multipeLines){
            params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, context.getResources().getDimensionPixelSize(R.dimen.multiple_height));
        }else{
            params=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        child.setLayoutParams(params);
        child.setGravity(Gravity.CENTER_VERTICAL);
        child.setShowDividers(SHOW_DIVIDER_BEGINNING|SHOW_DIVIDER_MIDDLE|SHOW_DIVIDER_END);
        child.setDividerDrawable(context.getResources().getDrawable(R.drawable.light_divider_line));
        layout_content.addView(child);
        return child;
    }

    EditText edit;
    RadioGroup rbs;
    ArrayList<CheckBox> arrayListCbs;
    String viewType;
    JSONObject infos;
    String sp_selections[];
    String sp_select;
    public JSONObject getVal(){
        try {
                if(viewType.equals(typeText)||viewType.equals(typeTextArea)){
                    if(edit!=null){
                        JSONObject ob=new JSONObject();
                        String val=((EditText)edit).getText().toString();
                        ob.put(infos.getString(CONTENT_JSONNAME),val);
                        return ob;
                    }
                }else if(viewType.equals(typeRadio)){
                    if(rbs!=null){
                        JSONArray ja=infos.getJSONArray(CONTENT_SELECTIONS);
                        for(int i=0;i<rbs.getChildCount();i++){
                            RadioButton rb=(RadioButton)rbs.getChildAt(i);
                            if(rb.isChecked()){
                                JSONObject ob=new JSONObject();
                                String val=ja.getString(i);
                                ob.put(infos.getString(CONTENT_JSONNAME),val);
                                return ob;
                            }
                        }
                    }

                }else if(viewType.equals(typeCheckbox)){
                    if(arrayListCbs!=null&&arrayListCbs.size()!=0){
                        JSONObject ob=new JSONObject();
                        JSONArray ar=new JSONArray();
                        for(int i=0;i<arrayListCbs.size();i++){
                            if(arrayListCbs.get(i).isChecked()){
                                ar.put(arrayListCbs.get(i).getText());
                            }
                        }
                        ob.put(infos.getString(CONTENT_JSONNAME),ar);
                        return ob;
                    }
                    return null;
                }else if(viewType.equals(typeSelect)){
                    if(sp_select!=null){
                        JSONObject ob=new JSONObject();
                        ob.put(infos.getString(CONTENT_JSONNAME),sp_select);
                        return ob;
                    }
                    return null;
                }else if(viewType.equals(typeTitle)){
                    //nothing.
                    return null;
                }
        }catch (Exception e){e.printStackTrace();}

        return null;
    }

    String findEditContent(JSONObject obj,JSONObject editJson){
        try {
            if(editJson!=null&&obj.has(CONTENT_JSONNAME)){
                JSONArray jax=editJson.getJSONArray(CONTENT_VAL);
                if(jax!=null){
                    for(int i=0;i<jax.length();i++){
                        JSONObject jox=jax.getJSONObject(i);
                        if(jox.has(obj.getString(CONTENT_JSONNAME))){
                            return jox.getString(obj.getString(CONTENT_JSONNAME));
                        }
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();}
        return null;
    }


    public void loadView(JSONObject obj, @Nullable JSONObject editJson, Context context, LinearLayout layout_content, boolean multipeLines){
        try{
            L.e("loadContent:"+obj.toString());

            LinearLayout child_layout=new LinearLayout(context);
            child_layout.setOrientation(LinearLayout.HORIZONTAL);
            LayoutParams child_params;
            if(multipeLines){
                child_params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, context.getResources().getDimensionPixelSize(R.dimen.multiple_height));
            }else{
                child_params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            }
            child_params.setMarginEnd(context.getResources().getDimensionPixelSize(R.dimen.basic_bt_margin));
            child_params.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.basic_bt_margin));
            child_layout.setLayoutParams(child_params);
            child_layout.setGravity(Gravity.CENTER_VERTICAL);
            layout_content.addView(child_layout);

            String type=obj.getString(CONTENT_TYPE);
            viewType=type;
            infos=obj;
            TextView desc=new TextView(context);
            desc.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimensionPixelSize(R.dimen.sizeNormalText));
            desc.setText(obj.getString(CONTENT_TITLE));
            LayoutParams params=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            desc.setLayoutParams(params);
            child_layout.addView(desc);
            if(typeTitle.equals(type)){//textview
                L.e("load text view.");

            }else if(typeText.equals(type)||typeTextArea.equals(type)){//textedit
                L.e("load edit view.");
                EditText text=new EditText(context);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimensionPixelSize(R.dimen.sizeMinText));
                text.setBackground(context.getResources().getDrawable(R.drawable.common_text_frame));
                text.setPadding(context.getResources().getDimensionPixelSize(R.dimen.sizeSmall),0,context.getResources().getDimensionPixelSize(R.dimen.sizeSmall),0);
                LayoutParams pm;
                text.setMinWidth(context.getResources().getDimensionPixelSize(R.dimen.double_content_width));
                if(typeTextArea.equals(type)){
                    text.setLines(4);
                    pm=new LinearLayout.LayoutParams(multipeLines?context.getResources().getDimensionPixelSize(R.dimen.edit_min_width):LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                }else{
                    text.setLines(1);
                    pm=new LinearLayout.LayoutParams(multipeLines?context.getResources().getDimensionPixelSize(R.dimen.double_content_width):LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.et_height_min));
                }
                text.setLayoutParams(pm);
                child_layout.addView(text);
                String val=findEditContent( obj, editJson);
                if(val!=null){
                    text.setText(val);
                }
                edit=text;
            }else if(typeRadio.equals(type)){
                L.e("load radio group view.");
                JSONArray radios=obj.getJSONArray(CONTENT_SELECTIONS);
                if(radios!=null){
                    RadioGroup mgroup=new RadioGroup(context);
                    mgroup.setOrientation(LinearLayout.HORIZONTAL);
                    for(int k=0;k<radios.length();k++){
                        RadioButton rb=new RadioButton(context);
                        rb.setText(radios.getString(k));
                        rb.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimensionPixelSize(R.dimen.sizeMinText));
                        mgroup.addView(rb);

                        String val=findEditContent( obj, editJson);
                        if(val!=null){
                            if(radios.getString(k).equals(val)){
                                rb.setChecked(true);
                            }
                        }

                    }
                    rbs=mgroup;
                    LayoutParams pm=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    mgroup.setLayoutParams(pm);
                    child_layout.addView(mgroup);
                }
            }else if(typeCheckbox.equals(type)){
                L.e("load checkbox group view.");

                JSONArray jcbs=obj.getJSONArray(CONTENT_SELECTIONS);
                if(jcbs!=null){
                    LinearLayout child_cb=new LinearLayout(context);
                    child_cb.setOrientation(LinearLayout.HORIZONTAL);
                    LayoutParams cb_params;
                    cb_params=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    child_cb.setLayoutParams(cb_params);
                    child_cb.setGravity(Gravity.CENTER_VERTICAL);
                    child_layout.addView(child_cb);

                    arrayListCbs=new ArrayList<>();
                    for(int k=0;k<jcbs.length();k++){
                        CheckBox cb=new CheckBox(context);
                        cb.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimensionPixelSize(R.dimen.sizeMinText));
                        cb.setText(jcbs.getString(k));
                        LayoutParams pm=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        cb.setLayoutParams(pm);
                        child_cb.addView(cb);
                        arrayListCbs.add(cb);
                        String val=findEditContent( obj, editJson);

                        if(val!=null){
                            try{
                                JSONArray jal=new JSONArray(val);
                                for(int i=0;i<jal.length();i++){
                                    if(jcbs.getString(k).equals(jal.getString(i))){
                                        cb.setChecked(true);
                                    }
                                }
                            }catch (Exception e){e.printStackTrace();}


                        }
                    }
                }
            }else if(typeSelect.equals(type)){
                JSONArray jsp=obj.getJSONArray(CONTENT_SELECTIONS);
                if(jsp!=null&&jsp.length()!=0){
                        Spinner sp=new Spinner(context);
                        LayoutParams pm=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        sp.setLayoutParams(pm);
                        int margin=context.getResources().getDimensionPixelSize(R.dimen.sizeStandard);
                        pm.setMargins(margin,margin,margin,margin);
                        child_layout.addView(sp);
                        sp_selections=new String[jsp.length()];
                        int select=-1;
                        for(int i=0;i<jsp.length();i++){
                            sp_selections[i]=jsp.getString(i);

                            String val=findEditContent( obj, editJson);
                            if(val!=null){
                                if(sp_selections[i].equals(val)){
                                    if(select<0)select=i;

                                }
                            }
                        }

                        ArrayAdapter<String> adapter=new  ArrayAdapter(context,android.R.layout.simple_spinner_item,sp_selections);
                        sp.setAdapter(adapter);
                    if(select>=0){
                        sp.setSelection(select,true);
                    }
                        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                sp_select=sp_selections[position];
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                sp_select=null;
                            }
                        });
                    }
            }
            //add vertical line
            //View view=new View(context);



        }catch (Exception e){e.printStackTrace();}
    }
}
