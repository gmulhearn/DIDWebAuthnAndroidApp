package com.gmulhearn.didwebauthn

import android.os.Build
import android.os.Bundle
import android.system.Os
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import org.hyperledger.indy.sdk.LibIndy

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StartFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Os.setenv("EXTERNAL_STORAGE", context?.filesDir?.absolutePath, true)
        println("external storage: ${Os.getenv("EXTERNAL_STORAGE")}")
        System.loadLibrary("indy")
        println("LOADED LIBRARY INDY")
        LibIndy.init()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_StartFragment_to_homeFragment)
        }
    }
}