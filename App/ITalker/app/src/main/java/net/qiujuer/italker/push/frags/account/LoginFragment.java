package net.qiujuer.italker.push.frags.account;


import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import net.qiujuer.genius.ui.widget.Loading;
import net.qiujuer.italker.common.app.PresenterFragment;
import net.qiujuer.italker.factory.presenter.account.LoginContract;
import net.qiujuer.italker.factory.presenter.account.LoginPresenter;
import net.qiujuer.italker.push.R;
import net.qiujuer.italker.push.activities.MainActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 登录的界面，
 * 使用了，MVP 架构。在这里，Fragment属于Presenter，抽象出Fragment的公有属性和方法。
 * 慢慢体会这样设计的优点，以及是否有可以改进的地方。
 */
public class LoginFragment extends PresenterFragment<LoginContract.Presenter>
        implements LoginContract.View {
    private AccountTrigger mAccountTrigger;

    @BindView(R.id.edit_phone)
    EditText mPhone;
    @BindView(R.id.edit_password)
    EditText mPassword;

    @BindView(R.id.loading)
    Loading mLoading;

    @BindView(R.id.btn_submit)
    Button mSubmit;

    /**
     * Fragment是属于MVP中的View，现在只配置了View，Presenter还没有配置。
     */
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 拿到我们的Activity的引用
        mAccountTrigger = (AccountTrigger) context;
    }

    /**
     * 这个是关键，初始化Presenter，在Fragment的onAttach之后初始化。
     * 所有的业务逻辑都放在这里。
     * @return
     */
    @Override
    protected LoginContract.Presenter initPresenter() {
        return new LoginPresenter(this);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_login;
    }


    @OnClick(R.id.btn_submit)
    void onSubmitClick() {
        String phone = mPhone.getText().toString();
        String password = mPassword.getText().toString();
        // 调用P层进行注册
        mPresenter.login(phone, password);
    }

    @OnClick(R.id.txt_go_register)
    void onShowRegisterClick() {
        // 让AccountActivity进行界面切换
        mAccountTrigger.triggerView();
    }

    @Override
    public void showError(int str) {
        super.showError(str);
        // 当需要显示错误的时候触发，一定是结束了

        // 停止Loading
        mLoading.stop();
        // 让控件可以输入
        mPhone.setEnabled(true);
        mPassword.setEnabled(true);
        // 提交按钮可以继续点击
        mSubmit.setEnabled(true);
    }

    @Override
    public void showLoading() {
        super.showLoading();

        // 正在进行时，正在进行注册，界面不可操作
        // 开始Loading
        mLoading.start();
        // 让控件不可以输入
        mPhone.setEnabled(false);
        mPassword.setEnabled(false);
        // 提交按钮不可以继续点击
        mSubmit.setEnabled(false);
    }

    /**
     * 所有也变的变化，都放在View中实现。
     */
    @Override
    public void loginSuccess() {
        MainActivity.show(getContext());
        getActivity().finish();
    }
}