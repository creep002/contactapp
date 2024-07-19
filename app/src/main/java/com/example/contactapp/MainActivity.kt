@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.contactapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.graphics.Color.Companion
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.contactapp.ui.theme.Blue
import com.example.contactapp.ui.theme.GreenJC
import com.example.contactapp.ui.theme.Lightgrayy
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(applicationContext,
            ContactDatabase::class.java, "contact_database").build() //Create database

        val repository = ContactRepository(database.contactDao()) //Create repository

        val viewModel: ContactViewModel by viewModels { //Create viewModel to communicate between View and Model
            ContactViewModel.ContactViewModelFactory(
                repository
            )
        }

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "contactList") { //Create navigation for all page screen, and startDestination is the main/home screen when we open the app
                composable("contactList") { ContactListScreen(viewModel, navController)}
                composable("addContact"){ AddContactScreen(viewModel, navController)}
                composable("searchbar") { SearchBarScreen(viewModel, navController) }
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
@Composable // This is for create a Card for a contact
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
            Text(contact.name, fontSize = 18.sp)
        }
    }
}

@Composable // Searching Page here
fun SearchBarScreen(viewModel: ContactViewModel, navController: NavController){
    var searchText by remember { mutableStateOf("") }

    val contacts by viewModel.allContacts.observeAsState(initial = emptyList())

    val filteredContacts = contacts.filter { contact ->
        contact.name.contains(searchText, ignoreCase = true) || contact.phoneNumber.contains(searchText)
    }

    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier.height(48.dp),
                title = {
                    Box(modifier = Modifier.fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically)) {
                        Text("Search Contacts", fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Companion.White,
                    titleContentColor = Companion.White,
                    navigationIconContentColor = Companion.Black
                ))
        },
        containerColor = Companion.White
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {

            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Searching") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            LazyColumn {
                items(filteredContacts) { contact ->
                    SearchContactItem(contact = contact, searchText = searchText) {
                        navController.navigate("contactDetail/${contact.id}")
                    }
                }
            }
        }
    }
}


@Composable// Here for ssearching contactItem
fun SearchContactItem(contact: Contact, searchText: String, onClick: () -> Unit) {
    val annotatedString = remember(contact.name, searchText) {
        buildAnnotatedString {
            val name = contact.name
            if (searchText.isEmpty()) {
                append(name)
            } else {
                var lastIndex = 0
                val regex = Regex("(?i)$searchText")
                regex.findAll(name).forEach { matchResult ->
                    val startIndex = matchResult.range.first
                    append(name.substring(lastIndex, startIndex))
                    withStyle(style = SpanStyle(color = Companion.Red)) {
                        append(name.substring(startIndex, startIndex + searchText.length))
                    }
                    lastIndex = startIndex + searchText.length
                }
                append(name.substring(lastIndex))
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Companion.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(contact.image),
                contentDescription = contact.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = annotatedString)
                Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


//Here is the main screen, displaying all contacts here
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContactListScreen(viewModel: ContactViewModel, navController: NavController) {
    val context = LocalContext.current.applicationContext

    Scaffold(
        topBar = { //The app top bar for contactlistscreen
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight()
                            .wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Text("Contacts", fontSize = 22.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Contacts", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.contacticon),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    // Searching Bar
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            onClick = {// here for move to search bar page
                                navController.navigate("searchbar")
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    titleContentColor = Companion.Black,
                    navigationIconContentColor = Companion.Black
                )
            )
        },
        floatingActionButton = { // Here for move to add contact page
            FloatingActionButton(containerColor = GreenJC, onClick = {
                navController.navigate("addContact")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = Lightgrayy// background color scaffold here
    ) {paddingValues ->
        val contacts by viewModel.allContacts.observeAsState(initial = emptyList())
        val favoriteContacts by viewModel.favoriteContacts.observeAsState(initial = emptyList())
        val nonFavoriteContacts by viewModel.nonFavoriteContacts.observeAsState(initial = emptyList())

        // sorted contacts by name from a to z
        val sortedContacts = contacts.sortedBy { it.name }

        // sorted non favorite contacts
        val sortedNonFavoriteContacts = nonFavoriteContacts.sortedBy { it.name }

        // Grouped not in favorite field
        val groupedNonFavoriteContacts = sortedNonFavoriteContacts.groupBy { contact ->
            val firstChar = contact.name.firstOrNull()?.lowercaseChar()
            if (firstChar == null || firstChar.isDigit()) {
                "#"
            } else {
                firstChar.toString()
            }
        }

        val groupedContacts = sortedContacts.groupBy { contact ->
            val firstChar = contact.name.firstOrNull()?.lowercaseChar()
            if (firstChar == null || firstChar.isDigit()) {
                "#"
            } else {
                firstChar.toString()
            }
        }

        // Sorted key from a to z and #
        val sortedKeys = groupedNonFavoriteContacts.keys.sortedBy { key ->
            if (key == "#") {
                "zzz" // Put "#" to the last list
            } else {
                key
            }
        }
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically){
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "The Favorite Contact",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
            LazyColumn {
                items(favoriteContacts) { contact ->
                    ContactItem(contact = contact) {
                        navController.navigate("contactDetail/${contact.id}")
                    }
                }
            }
            LazyColumn {
                // Displaying non favorite contacts
                sortedKeys.forEach { groupKey ->
                    item {
                        Text(
                            text = groupKey.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                    items(groupedNonFavoriteContacts[groupKey] ?: emptyList()) { contact ->
                        ContactItem(contact = contact) {
                            navController.navigate("contactDetail/${contact.id}")
                        }
                    }
                }
            }
        }
    }
}

//Add contact Screen page here
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "DiscouragedApi")
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
                        Text(text = "Add Contact", fontSize = 22.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {Toast.makeText(context, "Add Contact", Toast.LENGTH_SHORT).show() }) {
                    Icon(painter = painterResource(id = R.drawable.addcontact), contentDescription = null)
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    titleContentColor = Companion.Black,
                    navigationIconContentColor = Companion.Black
                    )
                )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Companion.Transparent, // Make the background transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Add padding to ensure text doesn't touch the edges
                    horizontalArrangement = Arrangement.SpaceAround, // Center the items
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Companion.Transparent,
                            contentColor = Companion.Black
                        ),
                        onClick = { navController.navigateUp() }
                    ) {
                        Text("Back", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }
                    Button(onClick = {//This here for who don't choose the image. The function will automatically add a human.png,
                        val firstChar = name.firstOrNull()?.lowercaseChar()
                        val defaultImageResId = if (firstChar == null || firstChar.isDigit()) {
                            context.resources.getIdentifier("human", "drawable", context.packageName)
                        } else {
                            context.resources.getIdentifier(firstChar.toString(), "drawable", context.packageName)
                        }

                        val imagePath = imageUri?.let {// Here for who choose the image
                            copyUriToInternalStorage(context, it, "$name.jpg")
                        } ?: "android.resource://${context.packageName}/$defaultImageResId"

                        imagePath.let { path ->
                            viewModel.addContact(path, name, phonenumber, email)
                            navController.navigate("contactList") {
                                popUpTo(0)
                            }
                        }
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Companion.Transparent,
                            contentColor = Companion.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 20.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }
                }
            }
        },
        containerColor = Lightgrayy,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            imageUri?.let { uri ->
                Image(painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(GreenJC)) {
                Text(text = "Choose Image", color = Companion.Black)
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextField(value = name, onValueChange = { name = it },
                label = { Text(text = "Name") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
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

            TextField(
                value = phonenumber,
                onValueChange = { phonenumber = it },
                label = { Text(text = "Phone Number") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone Icon")
                },
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

            TextField(value = email, onValueChange = { email = it },
                label = { Text(text = "Email") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
                },
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
        }
    }
}
@Composable
fun ContactDetailScreen(contact: Contact, viewModel: ContactViewModel, navController: NavController) {
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
                        Text(text = "Contact Details", fontSize = 22.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(contact) }) {
                        Icon(
                            imageVector = if (contact.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    titleContentColor = Companion.White,
                    navigationIconContentColor = Companion.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = GreenJC,
                onClick = { navController.navigate("editContact/${contact.id}") }
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Contact")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(Companion.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(contact.image),
                        contentDescription = contact.name,
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(Companion.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = "Name")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Name", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.name, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(Companion.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Phone, contentDescription = "Phone")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Phone", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.phoneNumber, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(Companion.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Email, contentDescription = "Email")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Email", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.email, fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors(GreenJC),
                onClick = {
                    viewModel.deleteContact(contact)
                    navController.navigate("contactList") {
                        popUpTo(0)
                    }
                }
            ) {
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
                        Text(text = "Edit Contact", fontSize = 22.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {Toast.makeText(context, "Edit Contact", Toast.LENGTH_SHORT).show() }) {
                        Icon(painter = painterResource(id = R.drawable.contacticon), contentDescription = null)
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    titleContentColor = Companion.Black,
                    navigationIconContentColor = Companion.Black
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Companion.Transparent, // Make the background transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Add padding to ensure text does not touch edges
                    horizontalArrangement = Arrangement.SpaceAround, // Center the items
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Companion.Transparent, // Make button background transparent
                            contentColor = Companion.Black // Adjust text color if needed
                        ),
                        onClick = { navController.navigateUp() }
                    ) {
                        Text("Back", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }
                    Button(onClick = {
                        val updateContact = contact.copy(image = imageUri, name = name, phoneNumber = phonenumber, email = email)
                        viewModel.updateContact(updateContact)
                        navController.navigate("contactList"){
                            popUpTo(0)
                        }
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Companion.Transparent, // Make button background transparent
                            contentColor = Companion.Black // Adjust text color if needed
                        ),
                        modifier = Modifier
                            .weight(1f) // Make the button take up available space
                            .padding(start = 20.dp) // Add some space between buttons
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }
                }
            }
        },
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

            TextField(value = name,
                onValueChange = {name = it},
                label = {Text(text = "Name")},
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
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

            TextField(
                value = phonenumber,
                onValueChange = {phonenumber = it},
                label = {Text(text = "Phone Number")},
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                },
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

            TextField(
                value = email,
                onValueChange = {email = it},
                label = {Text(text = "Email")},
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
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
        }
    }
}


//Here is for adding the image to the internalStorage
fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {

    val file = File(context.filesDir, fileName)
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file.absolutePath
    } catch (e: Exception) { //Show error if failed
        e.printStackTrace()
        null
    }
}



