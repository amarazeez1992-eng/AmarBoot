package com.personal.gridbot.amaros.accounts

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
    var brokerName by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.DEMO) }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("حسابات التداول")
            Text("إضافة الحساب مصممة للربط الحقيقي لاحقاً عبر موصل الوسيط أو منصة التداول.")
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = brokerName,
                        onValueChange = { brokerName = it },
                        label = { Text("اسم الوسيط") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = if (accountType == AccountType.REAL) "حقيقي" else "تجريبي",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("نوع الحساب") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("تجريبي") }, onClick = { accountType = AccountType.DEMO; expanded = false })
                            DropdownMenuItem(text = { Text("حقيقي") }, onClick = { accountType = AccountType.REAL; expanded = false })
                        }
                    }
                    OutlinedTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = { Text("خادم الحساب") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        label = { Text("رقم الدخول") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            val accountId = UUID.randomUUID().toString()
                            val chars = password.toCharArray()
                            vault.savePassword(accountId, chars)
                            password = ""
                            message = "تم تجهيز بيانات الحساب بشكل مشفر للحساب: $accountId"
                        },
                        enabled = brokerName.isNotBlank() && server.isNotBlank() && login.isNotBlank() && password.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("حفظ الحساب بأمان")
                    }
                    if (message.isNotBlank()) Text(message)
                }
            }
        }
        item {
            Text("حماية مهمة: التطبيق لا يرسل بيانات الحساب إلى أي وسيط من هذه الشاشة وحدها، ولا يفعّل التداول الحقيقي تلقائياً.")
        }
    }
}

@Suppress("UNUSED_PARAMETER")
private fun vaultFor(context: Context) = AmarAccountVault(context)
