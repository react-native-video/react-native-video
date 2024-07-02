#import <Foundation/Foundation.h>
#import "AVKit/AVKit.h"

@protocol RCTVideoPlayerViewControllerDelegate <NSObject>
- (void)videoPlayerViewControllerWillDismiss:(AVPlayerViewController *)playerViewController;
- (void)videoPlayerViewControllerDidDismiss:(AVPlayerViewController *)playerViewController;
@end
