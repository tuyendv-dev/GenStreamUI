package network.ermis.genstreamui.common.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import network.ermis.genstreamui.R

/**
 * Base Fragment dùng ViewBinding, quản lý binding nullable an toàn (giải phóng ở onDestroyView).
 * Truyền hàm inflate của binding, ví dụ:
 *   class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate)
 *
 * Vòng đời chuẩn: initViews() -> onClickViews() -> observerData() trong onViewCreated().
 * Tương đương BaseFragment<VB: ViewDataBinding> của GenPlayAndroid, đổi sang ViewBinding.
 */
abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    private var loadingDialog: Dialog? = null

    /** Hiện dialog loading toàn màn (không huỷ được) trong lúc chờ API. An toàn khi gọi nhiều lần. */
    protected fun showLoading() {
        val dialog = loadingDialog ?: Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }.also { loadingDialog = it }
        if (!dialog.isShowing) dialog.show()
    }

    /** Ẩn dialog loading nếu đang hiện. */
    protected fun hideLoading() {
        loadingDialog?.takeIf { it.isShowing }?.dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        onClickViews()
        observerData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideLoading()
        loadingDialog = null
        _binding = null
    }

    open fun initViews() {}
    open fun onClickViews() {}
    open fun observerData() {}
}
