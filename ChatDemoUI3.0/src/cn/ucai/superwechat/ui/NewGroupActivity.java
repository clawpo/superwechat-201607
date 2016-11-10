/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

public class NewGroupActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView mImgBack;
    @BindView(R.id.txt_title)
    TextView mTxtTitle;
    @BindView(R.id.txt_right)
    TextView mTxtRight;
    @BindView(R.id.edit_group_name)
    EditText mEditGroupName;
    @BindView(R.id.edit_group_introduction)
    EditText mEditGroupIntroduction;
    @BindView(R.id.iv_avatar)
    ImageView mIvAvatar;
    @BindView(R.id.cb_public)
    CheckBox mCbPublic;
    @BindView(R.id.second_desc)
    TextView mSecondDesc;
    @BindView(R.id.cb_member_inviter)
    CheckBox mCbMemberInviter;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_new_group);
        ButterKnife.bind(this);

        initView();
        setListener();

    }

    private void setListener() {
        mCbPublic.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSecondDesc.setText(R.string.join_need_owner_approval);
                } else {
                    mSecondDesc.setText(R.string.Open_group_members_invited);
                }
            }
        });
    }

    private void initView() {
        mImgBack.setVisibility(View.VISIBLE);
        mTxtTitle.setVisibility(View.VISIBLE);
        mTxtTitle.setText(getString(R.string.The_new_group_chat));
        mTxtRight.setVisibility(View.VISIBLE);
        mTxtRight.setText(getString(R.string.button_save));
    }

    public void save() {
        String name = mEditGroupName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Group_name_cannot_be_empty).show();
        } else {
            // select from contact list
            startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
        final String st2 = getResources().getString(R.string.Failed_to_create_groups);
        if (resultCode == RESULT_OK) {
            //new group
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(st1);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String groupName = mEditGroupName.getText().toString().trim();
                    String desc = mEditGroupIntroduction.getText().toString();
                    String[] members = data.getStringArrayExtra("newmembers");
                    try {
                        EMGroupOptions option = new EMGroupOptions();
                        option.maxUsers = 200;

                        String reason = NewGroupActivity.this.getString(R.string.invite_join_group);
                        reason = EMClient.getInstance().getCurrentUser() + reason + groupName;

                        if (mCbPublic.isChecked()) {
                            option.style = mCbMemberInviter.isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
                        } else {
                            option.style = mCbMemberInviter.isChecked() ? EMGroupStyle.EMGroupStylePrivateMemberCanInvite : EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                        }
                        EMGroup emGroup = EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);
                        emGroup.getGroupId();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    } catch (final HyphenateException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            }).start();
        }
    }

    public void back(View view) {
        finish();
    }

    @OnClick({R.id.img_back, R.id.txt_right, R.id.layout_group_icon, R.id.cb_public, R.id.cb_member_inviter})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                MFGT.finish(this);
                break;
            case R.id.txt_right:
                save();
                break;
            case R.id.layout_group_icon:
                break;
            case R.id.cb_public:
                break;
            case R.id.cb_member_inviter:
                break;
        }
    }
}
