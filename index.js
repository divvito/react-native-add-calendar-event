import { NativeModules, Platform, PermissionsAndroid } from 'react-native';

const AddCalendarEvent = NativeModules.AddCalendarEvent;

export const presentNewCalendarEventDialog = eventConfig => {
  return AddCalendarEvent.presentNewEventDialog(eventConfig);
};
