@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.contactapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.contactapp.ui.theme.ContactAppTheme
import com.example.contactapp.ui.theme.GreenJC
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(applicationContext,
            ContactDatabase::class.java, "contact_database").build()

        val repository = ContactRepository(database.contactDao())

        val viewModel: ContactViewModel by viewModels {
            ContactViewModel.ContactViewModelFactory(
                repository
            )
        }

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "contactList") {
                composable("contactList") { ContactListScreen(viewModel, navController)}
                composable("addContact"){ AddContactScreen(viewModel, navController)}
                composable("contactDetail/{contactId}"){backStackEntry ->
                    val contactId = backStackEntry.arguments?.getString("contactId")?.toInt()
                    val contact = viewModel.allContacts.observeAsState(initial = emptyList()).value.find { it.id == contactId }
                    contact?.let { ContactDetailScreen(it, viewModel, navController) }
                }
                composable("editContact/{contactId}"){backStackEntry ->
                    val contactId = backStackEntry.arguments?.getString("contactId")?.toInt()
                    val contact = viewModel.allContacts.observeAsState(initial = emptyList()).value.find { it.id == contactId }
                    contact?.let { EditContactScreen(it, viewModel, navController) }
                }
            }
        }
    }
}
@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Companion.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Image(painter = rememberAsyncImagePainter(contact.image),
                contentDescription = contact.name,
                modifier = Modifier.size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(16.dp))
            Text(contact.name)
        }
    }
}

@Composable
fun ContactListScreen(viewModel: ContactViewModel, navController: NavController){
    val context = LocalContext.current.applicationContext

    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier.height(48.dp),
                title = {
                    Box(modifier = Modifier.fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically)) {
                        Text("Contacts", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {Toast.makeText(context, "Contacts", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(painter = painterResource(id = R.drawable.contactdetails), contentDescription = null)
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Companion.Red,
                    titleContentColor = Companion.White,
                    navigationIconContentColor = Companion.White
                ))
        },
        floatingActionButton = {
            FloatingActionButton(containerColor = GreenJC, onClick = { navController.navigate("addContact")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contacts")
            }
        }
    ) {paddingValues ->
        val contacts by viewModel.allContacts.observeAsState(initial = emptyList())
        LazyColumn(modifier = Modifier.padding(paddingValues)
            .padding(16.dp)
        ) {
            items(contacts){ contact ->
                ContactItem(contact = contact) {
                    navController.navigate("contactDetail/${contact.id}")
                }

            }
        }

    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddContactScreen(viewModel: ContactViewModel, navController: NavController)
{
    val context = LocalContext.current.applicationContext

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var name by remember {
        mutableStateOf("")
    }
    var phonenumber by remember {
        mutableStateOf("")
    }
    var email by remember {
        mutableStateOf("")
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {uri: Uri? ->
        imageUri = uri

    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text(text = "Add Contact", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {Toast.makeText(context, "Add Contact", Toast.LENGTH_SHORT).show() }) {
                    Icon(painter = painterResource(id = R.drawable.addcontact), contentDescription = null)
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Companion.White,
                    navigationIconContentColor = Companion.White
                    )
                )
        }
    ){paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment =  Alignment.CenterHorizontally
                ) {
                    imageUri?.let { uri ->
                        Image(painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop)
                    }
                    Spacer(modifier =  Modifier.height(12.dp))

                    Button(onClick = { launcher.launch("image/*")},
                        colors = ButtonDefaults.buttonColors(GreenJC)) {
                        Text(text = "Choose Image")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(value = name, onValueChange = {name = it},
                        label = {Text(text = "Name")},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Companion.White,
                            unfocusedContainerColor = Companion.White,
                            focusedTextColor = Companion.Black,
                            unfocusedTextColor = Companion.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(value = phonenumber, onValueChange = {phonenumber = it},
                        label = {Text(text = "Phone Number")},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Companion.White,
                            unfocusedContainerColor = Companion.White,
                            focusedTextColor = Companion.Black,
                            unfocusedTextColor = Companion.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(value = email, onValueChange = {email = it},
                        label = {Text(text = "Email")},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Companion.White,
                            unfocusedContainerColor = Companion.White,
                            focusedTextColor = Companion.Black,
                            unfocusedTextColor = Companion.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        imageUri?.let {
                            val internalPath = copyUriToInternalStorage(context, it, "$name.jpg")
                            internalPath?.let { path ->
                                viewModel.addContact(path, name, phonenumber, email)
                                navController.navigate("contactList"){
                                    popUpTo(0)
                                }
                            }
                        }
                    }, colors = ButtonDefaults.buttonColors(GreenJC)){
                        Text(text = "Add Contact")
                    }
                }
            }
        }
@Composable
fun ContactDetailScreen(contact: Contact, viewModel: ContactViewModel, navController: NavController){
    val context = LocalContext.current.applicationContext

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text(text = "Contact Details1", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {Toast.makeText(context, "Contact Details", Toast.LENGTH_SHORT).show() }) {
                        Icon(painter = painterResource(id = R.drawable.contactdetails), contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Companion.White,
                    navigationIconContentColor = Companion.White
                )
            )
        }, floatingActionButton = {
            FloatingActionButton(containerColor = GreenJC, onClick = {navController.navigate("editContact/${contact.id}")
            }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Contact")
            }
        }
    ) {paddingValues ->
        Column(modifier = Modifier.fillMaxWidth()
            .padding(paddingValues)
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Card(modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
                colors = CardDefaults.cardColors(Companion.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = rememberAsyncImagePainter(contact.image), contentDescription = contact.name,
                        modifier = Modifier.size(128.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                        colors = CardDefaults.cardColors(Companion.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ){
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically){
                            Text("Name", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.name, fontSize = 16.sp)
                        }
                    }
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                        colors = CardDefaults.cardColors(Companion.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ){
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically){
                            Text("Phone", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.phoneNumber, fontSize = 16.sp)
                        }
                    }
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                        colors = CardDefaults.cardColors(Companion.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ){
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically){
                            Text("Email", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.email, fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(colors = ButtonDefaults.buttonColors(GreenJC),
                onClick = {viewModel.deleteContact(contact)
                navController.navigate("contactList"){
                    popUpTo(0)
                }
                }) {
                   Text("Delete Contact")
            }
        }

    }
}

@Composable
fun EditContactScreen(contact: Contact, viewModel: ContactViewModel, navController: NavController){
    val context = LocalContext.current.applicationContext

    var imageUri by remember {
        mutableStateOf(contact.image)
    }
    var name by remember {
        mutableStateOf(contact.name)
    }
    var phonenumber by remember {
        mutableStateOf(contact.phoneNumber)
    }
    var email by remember {
        mutableStateOf(contact.email)
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {uri: Uri? ->
        uri?.let { newUri ->
            val internalPath = copyUriToInternalStorage(context, newUri, "$name.jpg")
            internalPath?.let { path -> imageUri = path }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text(text = "Edit Contact", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {Toast.makeText(context, "Edit Contact", Toast.LENGTH_SHORT).show() }) {
                        Icon(painter = painterResource(id = R.drawable.editcontact), contentDescription = null)
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Companion.White,
                    navigationIconContentColor = Companion.White
                )
            )
        }
    ){paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment =  Alignment.CenterHorizontally
        ) {
                Image(painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop)

            Spacer(modifier =  Modifier.height(12.dp))

            Button(onClick = { launcher.launch("image/*")},
                colors = ButtonDefaults.buttonColors(GreenJC)) {
                Text(text = "Choose Image")
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextField(value = name, onValueChange = {name = it},
                label = {Text(text = "Name")},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Companion.White,
                    unfocusedContainerColor = Companion.White,
                    focusedTextColor = Companion.Black,
                    unfocusedTextColor = Companion.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(value = phonenumber, onValueChange = {phonenumber = it},
                label = {Text(text = "Phone Number")},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Companion.White,
                    unfocusedContainerColor = Companion.White,
                    focusedTextColor = Companion.Black,
                    unfocusedTextColor = Companion.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(value = email, onValueChange = {email = it},
                label = {Text(text = "Email")},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Companion.White,
                    unfocusedContainerColor = Companion.White,
                    focusedTextColor = Companion.Black,
                    unfocusedTextColor = Companion.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val updateContact = contact.copy(image = imageUri, name = name, phoneNumber = phonenumber, email = email)
                viewModel.updateContact(updateContact)
                navController.navigate("contactList"){
                    popUpTo(0)
                }
            }, colors = ButtonDefaults.buttonColors(GreenJC)){
                Text(text = "Update Contact")
            }
        }
    }
}

fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String{

    val file = File(context.filesDir, fileName)
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }.toString()
}
