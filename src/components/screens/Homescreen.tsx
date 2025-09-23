import { StyleSheet, Text, Touchable, TouchableOpacity, View, NativeModules, Image, Alert, Platform, PermissionsAndroid } from 'react-native'
import React from 'react'
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const Homescreen = () => {

    const insets = useSafeAreaInsets();
    const { MyNativeModule, CameraModule } = NativeModules;


    const handleAlert = () => {
        MyNativeModule.showNetWorkErrorToast();
    }

    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', marginTop: insets.top, marginBottom: insets.bottom }}>
            <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', gap: 20 }}>

                <TouchableOpacity onPress={handleAlert} style={{
                    backgroundColor: 'yellow', padding: 10, borderRadius: 5, marginBottom: 20, paddingHorizontal: 20,
                    shadowColor: "#000",
                    shadowOpacity: 0.25,
                    shadowRadius: 3.84,
                    elevation: 4

                }}>
                    <Text style={{ color: 'black' }}>Upload</Text>
                </TouchableOpacity>
            </View>
        </View>


    )
}

export default Homescreen

const styles = StyleSheet.create({})