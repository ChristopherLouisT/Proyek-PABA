package paba.proyek.proyekpaba

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AddPlayerDialogFragment : DialogFragment() {

    private val db = Firebase.firestore

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_player, null)

        val etName = view.findViewById<EditText>(R.id.etPlayerName)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnAddPlayer)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name required"
                return@setOnClickListener
            }

            savePlayer(name)
            dialog.dismiss()
        }

        return dialog
    }

    private fun savePlayer(name: String) {
        val player = hashMapOf(
            "name" to name,
            "totalMatch" to 0,
            "win" to 0,
            "lose" to 0
        )

        db.collection("players")
            .add(player)
    }
}
