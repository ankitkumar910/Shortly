

import bg4 from './assets/bg4.png'
import bg5 from './assets/bg5.jpg'
import bg6 from './assets/bg6.jpg'

export const backgroundImages = [ bg4, bg5, bg6]

export function getRandomBackground() {
  const randomIndex = Math.floor(Math.random() * backgroundImages.length)
  return backgroundImages[randomIndex]
}
