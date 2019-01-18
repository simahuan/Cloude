package com.pisen.router.ui.phone.settings;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.studio.os.PreferencesUtils;
import android.studio.view.widget.BaseAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.pisen.router.R;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.ui.base.NavigationFragment;
import com.pisen.router.ui.base.impl.DefaultNavigationBar;

/**
 * 修改头像
 * @author  mahuan
 * @version 1.0 2015年5月27日 下午4:05:15
 */
public class ModifyHeadFragment extends NavigationFragment{
	private static final int DEFAULT_HEAD_INDEX = 0;
	private GridView  gridView;
	private EditText  edtNickName;
	private ImageView imgChangeView;
	
	@Override
	protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		initNavigationBar();
		hideSoftInputFromWindow();
		return inflater.inflate(R.layout.cloud_settings_modify_head, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		gridView = (GridView) getView().findViewById(R.id.gridView);
		edtNickName = (EditText)getView().findViewById(R.id.edtNickName);
		edtNickName.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		edtNickName.addTextChangedListener(renameTextWatcher);
		
		imgChangeView = (ImageView) getView().findViewById(R.id.imgChangeHead);
		final GridViewAdapte adapte = new GridViewAdapte(getActivity());
		adapte.setData(IconResource.getAllIcons().keySet());
		gridView.setAdapter(adapte);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	int iconHeadKey = (Integer) parent.getItemAtPosition(position);
            	setNickHead(imgChangeView,iconHeadKey);
            	adapte.setSelected(position);
            }
        });
	}
	
	public void initNavigationBar(){
		DefaultNavigationBar navibar = (DefaultNavigationBar) getNavigationBar();
		navibar.setTitle("修改头像");
		navibar.setLeftButton("返回", new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideSoftInputFromWindow();
				getActivity().onBackPressed();
			}
		});
	}
	
	TextWatcher renameTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {
			    setNickName(edtNickName.getText().toString().trim());
				edtNickName.setGravity(Gravity.CENTER_HORIZONTAL);
		}
	};
	
	private void hideSoftInputFromWindow(){
		final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		View view = getActivity().getCurrentFocus();
		if (imm.isActive() &&  view!= null){
			imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle bundle) {
		view.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		super.onViewCreated(view, bundle);
	}
	
	class GridViewAdapte extends BaseAdapter<Integer>{
		private int selected = PreferencesUtils.getInt(KeyUtils.NICK_HEAD_INDEX, DEFAULT_HEAD_INDEX);
		public GridViewAdapte(Context context) {
			super(context);
		}
		
		public void setSelected(int index){
			this.selected = index;
			PreferencesUtils.setInt(KeyUtils.NICK_HEAD_INDEX, index);
			notifyDataSetChanged();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder holder = null;
			if(view == null){
				view = LayoutInflater.from(getActivity()).inflate(R.layout.cloud_settings_modify_head_item, (ViewGroup)null);
			    view.setTag(holder = new ViewHolder());
				holder.imgIconHead = (ImageView) view.findViewById(R.id.imgIconHead);
				holder.imgHeadBg = (ImageView) view.findViewById(R.id.imgHeadBg);
			}else {
				holder = (ViewHolder) view.getTag();
			}
			holder.imgIconHead.setImageResource(IconResource.getIcon(getItem(position)));
			if (position == selected){
				 holder.imgHeadBg.setVisibility(View.VISIBLE);
			}else{
				 holder.imgHeadBg.setVisibility(View.INVISIBLE);
			}
			return view;
		}
		class ViewHolder{
			ImageView imgIconHead,imgHeadBg;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		setNickName(edtNickName.getText().toString().trim());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setCursorShown();
		setCursorHidden();
		getNickName(edtNickName);
		getNickHead(imgChangeView);
	}
	
	/**
	 * @des   设置呢称
	 * @param nickName
	 */
	public void setNickName(String nickName){
		if (!TextUtils.isEmpty(nickName)){
			PreferencesUtils.setString(KeyUtils.NICK_NAME, nickName);
		} else {
			PreferencesUtils.setString(KeyUtils.NICK_NAME, null);
		}
	}
	
	/**
	 * @des 获取呢称
	 */
	public void getNickName(EditText edtNickName){
		final String name = PreferencesUtils.getString(KeyUtils.NICK_NAME,null);
		if (!TextUtils.isEmpty(name)){
			edtNickName.setText(name);
			edtNickName.setGravity(Gravity.CENTER_HORIZONTAL);
		} else {
			edtNickName.setHint(getResources().getString(R.string.settings_pls_input_nickname));
		}
	}
	
	/**
	 * @des 获取人物头像
	 */
	public void getNickHead(ImageView imgChangeHead){
	   imgChangeHead.setImageResource(IconResource.getIcon(PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1)));
	}
	
	/**
	 * @des   设置人物头像
	 * @param imgChangeView
	 * @param iconHeadKey
	 */
	public void setNickHead(ImageView imgChangeView,int iconHeadKey){
		imgChangeView.setImageResource(IconResource.getIcon(iconHeadKey));
		PreferencesUtils.setInt(KeyUtils.NICK_HEAD, iconHeadKey);
	}
	
	/**
	 * @des 隐藏光标
	 */
	private void setCursorHidden() {
		edtNickName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			    if (actionId == EditorInfo.IME_ACTION_DONE) {
			    	setEditable(edtNickName,false);
			    	edtNickName.setGravity(Gravity.CENTER_HORIZONTAL);
                }
				return false;
			}
		});
	}

	/**
	 * @des 显示光标
	 */
	private void setCursorShown() {
		edtNickName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setEditable(edtNickName, true);
			}
		});
	}
	
	private void setEditable(EditText mEdit, boolean value) {
		if (value) {
			mEdit.setCursorVisible(true);
			mEdit.setFocusableInTouchMode(true);
			mEdit.requestFocus();
		} else {
			mEdit.setCursorVisible(false);
		}
	}
}
