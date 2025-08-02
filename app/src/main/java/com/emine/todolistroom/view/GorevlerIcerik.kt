package com.emine.todolistroom.view


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.emine.todolistroom.databinding.FragmentGorevlerIcerikBinding
import com.emine.todolistroom.model.Icerik
import com.emine.todolistroom.roomdb.IcerikDAO
import com.emine.todolistroom.roomdb.IcerikDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException


class GorevlerIcerik : Fragment() {
    private var _binding: FragmentGorevlerIcerikBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //izin istemek için
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> //galeriye gitmek için
    private var secilenBitmap : Bitmap? = null
    private var secilenGorsel : Uri? = null

    private val mDisposabla= CompositeDisposable()

    private var secilenIcerik: Icerik? = null

    private lateinit var db: IcerikDatabase
    private lateinit var icerikDao: IcerikDAO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       registerLauncher()

        db = Room.databaseBuilder(
            requireContext(),
            IcerikDatabase::class.java, "Gorevler"
        ) //.allowMainThreadQueries()
            .build()
        icerikDao =db.icerikDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGorevlerIcerikBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener{gorselSec(it)}
        binding.kaydetButton.setOnClickListener{kaydet(it)}
        binding.silButton.setOnClickListener{sil(it)}

        arguments?.let {
            val bilgi= GorevlerIcerikArgs.Companion.fromBundle(it).bilgi
            if (bilgi=="yeni"){
                secilenIcerik=null
                //yeni eklenen görevler gösterilecek
                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled = true
                binding.gorevText.setText("")
                binding.gorevIcerikText.setText("")
            }else{
                //eski gorevler gösterilecek
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled = false
                val id= GorevlerIcerikArgs.fromBundle(it).id
                mDisposabla.add(
                    icerikDao.findByID(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            }
        }
    }

    private fun handleResponse(icerik: Icerik){
        val bitmap= BitmapFactory.decodeByteArray(icerik.gorsel,0,icerik.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.gorevText.setText(icerik.gorev)
        binding.gorevIcerikText.setText(icerik.icerik)

        secilenIcerik=icerik
    }

    fun kaydet(view: View){
        val gorev = binding.gorevText.text.toString()
        val icerik = binding.gorevIcerikText.text.toString()
        if (secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            val icerik = Icerik(gorev = gorev, icerik = icerik, gorsel = byteDizisi)

            mDisposabla.add(icerikDao.insert(icerik)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert))
        }


    }

    private fun handleResponseForInsert(){

        //önceki fragmenta dönecek
        val action= GorevlerIcerikDirections.actionGorevlerIcerikToGorevler()
        Navigation.findNavController(requireView()).navigate(action)
    }
    fun sil(view: View){
        if (secilenIcerik!=null){
            mDisposabla.add(
                icerikDao.delete(icerik = secilenIcerik!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
    }
    fun gorselSec(view: View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            if(ContextCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED){
                //İzin verilmemiş, izin istememiz gereki yor.
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    //snackbar göstermemiz lazım, kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin istememiz lazım//true dönerse
                    Snackbar.make(view,"Galeride görsel seçilmesi gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver",
                        View.OnClickListener{
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }else{
                    //Hayır dönerse izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //izin verilmiş, galeriye gidebiliriz
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
        else{
            if(ContextCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                //İzin verilmemiş, izin istememiz gerekiyor.
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //snackbar göstermemiz lazım, kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek izin istememiz lazım//true dönerse
                    Snackbar.make(view,"Galeride görsel seçilmesi gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver",
                        View.OnClickListener{
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                }else{
                    //Hayır dönerse izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //izin verilmiş, galeriye gidebiliriz
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }
    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorsel = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                secilenGorsel!!
                            )
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                secilenGorsel
                            )
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher=registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
                //izin verildi, galeriye gidebiliriz
            } else {
                //izin verilmedi
                Toast.makeText(requireContext(), "İzin verilmedi!", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int) : Bitmap {

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani: Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1) {
            // görselimiz yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        } else {
            //görselimiz dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width, height, true)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposabla.clear()
    }

}