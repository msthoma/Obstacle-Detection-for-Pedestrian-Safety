package cy.org.rise.obsai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cy.org.rise.obsai.R
import cy.org.rise.obsai.utils.toast
import kotlinx.android.synthetic.main.fragment_account.*

/**
 * Fragment that handles user login. Currently only for demonstration purposes
 */
class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button_sign_in.setOnClickListener {
            requireContext().toast(R.string.toast_not_implemented)
        }
    }
}
