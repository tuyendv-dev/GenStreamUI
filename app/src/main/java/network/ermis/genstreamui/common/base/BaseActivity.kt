package network.ermis.genstreamui.common.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * Base Activity dùng ViewBinding (không DataBinding).
 * Truyền hàm inflate của binding tương ứng, ví dụ:
 *   class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate)
 *
 * Vòng đời chuẩn: initViews() -> onClickViews() -> observerData().
 * Tương đương BaseActivity<VB: ViewDataBinding> của GenPlayAndroid, đổi sang ViewBinding.
 */
abstract class BaseActivity<VB : ViewBinding>(
    private val inflate: (LayoutInflater) -> VB
) : AppCompatActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        onClickViews()
        observerData()
    }

    open fun initViews() {}
    open fun onClickViews() {}
    open fun observerData() {}
}
