package com.personal.gridbot.amaros.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

private object AmarAppIdentityStore {
    private const val PREFS = "amar_app_identity_v1"
    fun save(context: Context, email: String, name: String, password: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val salt = p.getString("salt", null) ?: java.util.UUID.randomUUID().toString()
        p.edit().putString("email", email.trim()).putString("name", name.trim()).putString("salt", salt)
            .putString("passwordHash", hash(password, salt)).putBoolean("signedIn", true).apply()
    }
    fun email(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("email", "").orEmpty()
    fun name(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("name", "").orEmpty()
    fun signedIn(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("signedIn", false)
    fun signOut(context: Context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("signedIn", false).apply() }
    private fun hash(value: String, salt: String) = MessageDigest.getInstance("SHA-256")
        .digest((salt + value).toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

@Composable
fun AmarAppAccountCenterScreen() {
    val context = LocalContext.current
    var email by remember { mutableStateOf(AmarAppIdentityStore.email(context)) }
    var name by remember { mutableStateOf(AmarAppIdentityStore.name(context)) }
    var password by remember { mutableStateOf("") }
    var signedIn by remember { mutableStateOf(AmarAppIdentityStore.signedIn(context)) }
    var message by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF080812), Color(0xFF241044), Color(0xFF6A164F))), RoundedCornerShape(28.dp)).padding(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AMAR ID", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("حساب التطبيق عبر البريد الإلكتروني", color = Color(0xFFE9D5FF))
                    Text(if (signedIn) "● مسجل: ${email.ifBlank { "—" }}" else "○ لا يوجد حساب محلي نشط", color = Color(0xFFFFD166))
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("اسم الحساب") }, singleLine = true)
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("البريد الإلكتروني") }, singleLine = true)
                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("كلمة مرور الحساب") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                    Button(onClick = {
                        if (email.contains("@") && password.length >= 8) {
                            AmarAppIdentityStore.save(context, email, name, password)
                            password = ""; signedIn = true; message = "تم إنشاء هوية AMAR محلياً. الربط السحابي يحتاج خادم مصادقة معتمد." 
                        } else message = "أدخل بريداً صحيحاً وكلمة مرور من 8 أحرف على الأقل."
                    }, Modifier.fillMaxWidth(), enabled = email.isNotBlank() && password.isNotBlank()) { Text("إضافة / تسجيل الحساب") }
                    if (signedIn) OutlinedButton(onClick = { AmarAppIdentityStore.signOut(context); signedIn = false; message = "تم تسجيل الخروج على الجهاز." }, Modifier.fillMaxWidth()) { Text("تسجيل الخروج") }
                    if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item { Text("🔐 كلمة المرور لا تُعرض في الواجهة ولا تُرسل من هذه الطبقة. هذا المكوّن هو هوية محلية/طبقة استعداد للمصادقة السحابية، وليس ادعاءً بوجود خادم حسابات غير موجود.", style = MaterialTheme.typography.labelSmall) }
    }
}
