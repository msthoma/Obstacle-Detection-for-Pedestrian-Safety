package cy.org.rise.obsai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cy.org.rise.obsai.R
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
            Toast.makeText(context, getString(R.string.toast_not_implemented), Toast.LENGTH_LONG).show()
        }
    }
}
