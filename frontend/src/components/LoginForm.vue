<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4">
        <v-card class="elevation-12">
          <v-toolbar color="primary" dark flat>
            <v-toolbar-title>Labor Verwaltung - Login</v-toolbar-title>
          </v-toolbar>
          <v-card-text>
            <v-alert v-if="error" type="error" class="mb-4" closable>
              {{ error }}
            </v-alert>
            <v-form @submit.prevent="handleLogin">
              <v-text-field
                  v-model="username"
                  label="Benutzername"
                  prepend-icon="mdi-account"
                  required
              ></v-text-field>
              <v-text-field
                  v-model="password"
                  label="Passwort"
                  prepend-icon="mdi-lock"
                  type="password"
                  required
              ></v-text-field>
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="handleLogin" :loading="loading">
              Anmelden
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref } from "vue";
import AuthService from "../services/AuthService";

const emit = defineEmits(["login-success"]);

const username = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

const handleLogin = async () => {
  error.value = "";
  loading.value = true;

  try {
    const response = await AuthService.login(username.value, password.value);
    emit("login-success", response);
  } catch (e) {
    error.value = e.response?.data?.message || "Login fehlgeschlagen";
  }

  loading.value = false;
};
</script>