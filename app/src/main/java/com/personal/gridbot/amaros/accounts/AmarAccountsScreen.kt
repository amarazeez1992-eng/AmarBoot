package com.personal.gridbot.amaros.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun AmarAccountsScreen() {
    val context = LocalContext.current
    val vault = remember { AmarAccountVault(context) }
    val repository = remember { AmarAccountRepository(context) }
    var brokerName by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.DEMO) }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var accounts by remember { mutableStateOf(repository.list()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("حسابات التداول")
            Text("هذه الطبقة تحفظ تعريف الحساب محلياً، وتحفظ كلمة المرور مشفرة داخل مخزن مفاتيح أندرويد.")
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(brokerName, { brokerName = it }, label = { Text("اسم الوسيط") }, modifier = Modifier.fillMaxWidth())
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = if (accountType == AccountType.REAL) "حقيقي" else "تجريبي",
                            onValueChange = {}, readOnly = true,
                            label = { Text("نوع الحساب") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("تجريبي") }, onClick = { accountType = AccountType.DEMO; expanded = false })
                            DropdownMenuItem(text = { Text("حقيقي") }, onClick = { accountType = AccountType.REAL; expanded = false })
                        }
                    }
                    OutlinedTextField(server, { server = it }, label = { Text("خادم الحساب") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(login, { login = it }, label = { Text("رقم الدخول") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(password, { password = it }, label = { Text("كلمة المرور") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            val accountId = UUID.randomUUID().toString()
                            val credentialAlias = "حساب_$accountId"
                            repository.save(AmarAccountProfile(accountId, brokerName, accountType, server, login, credentialAlias))
                            vault.savePassword(accountId, password.toCharArray())
                            accounts = repository.list()
                            password = ""
                            message = "تم حفظ الحساب بأمان. لم يتم تفعيل الاتصال أو التداول الحقيقي."
                        },
                        enabled = brokerName.isNotBlank() && server.isNotBlank() && login.isNotBlank() && password.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("حفظ الحساب بأمان") }
                    if (message.isNotBlank()) Text(message)
                }
            }
        }
        item { Text("الحسابات المحفوظة: ${accounts.size}") }
        items(accounts, key = { it.id }) { account ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(account.brokerName)
                    Text("${account.login} • ${if (account.accountType == AccountType.REAL) "حقيقي" else "تجريبي"}")
                    Text(if (vault.hasPassword(account.id)) "بيانات الدخول محمية" else "بيانات الدخول غير مكتملة")
                }
            }
        }
        item {
            Text("حماية مهمة: وجود حساب حقيقي في التطبيق لا يعني تفعيل التنفيذ. التفعيل يحتاج موصل وسيط حقيقي، مصادقة، صلاحيات، وبوابة تداول مباشرة منفصلة.")
        }
    }
}
