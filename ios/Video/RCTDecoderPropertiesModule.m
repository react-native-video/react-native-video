#import "RCTDecoderPropertiesModule.h"

@implementation RCTDecoderPropertiesModule

RCT_EXPORT_MODULE(RNVDecoderPropertiesModule);

RCT_EXPORT_METHOD(getWidevineLevel : (RCTPromiseResolveBlock)resolve reject : (RCTPromiseRejectBlock)reject) {}

RCT_EXPORT_METHOD(isCodecSupported : (NSString*)mimeType width : (NSInteger*)width height : (NSInteger*)height) {}

RCT_EXPORT_METHOD(isHEVCSupported : (RCTPromiseResolveBlock)resolve reject : (RCTPromiseRejectBlock)reject) {}

@end
