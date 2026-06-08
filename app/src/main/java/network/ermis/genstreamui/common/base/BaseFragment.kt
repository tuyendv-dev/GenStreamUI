package network.ermis.genstreamui.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

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
        _binding = null
    }

    open fun initViews() {}
    open fun onClickViews() {}
    open fun observerData() {}
}
