// This guard prevent this file to be compiled in the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
#import <React/RCTViewComponentView.h>
#import <UIKit/UIKit.h>
#import "RCTVideo.h"

#ifndef RNCVideoComponentView_h
#define RNCVideoComponentView_h

NS_ASSUME_NONNULL_BEGIN

@interface RNCVideoComponentView : RCTViewComponentView <RCTVideoEventDelegate>
@end

NS_ASSUME_NONNULL_END

#endif /* RNCVideoComponentView_h */
#endif /* RCT_NEW_ARCH_ENABLED */
