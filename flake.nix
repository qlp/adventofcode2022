{
  description = "Built system for website";
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/release-22.05";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };

  outputs = { self, flake-parts, ... }:
    flake-parts.lib.mkFlake { inherit self; } {
      imports = [];
      systems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      perSystem = { config, self', inputs', pkgs, system, ... }: {
        packages = rec {
          default = aoc2022;
          aoc2022 = pkgs.stdenv.mkDerivation {
            buildInputs = with pkgs; [ coreutils openjdk17-bootstrap rsync ];
            src = ./.;
            name = "Advent of code 2022";
            buildPhase = ''
                ./build.sh
            '';

            installPhase = ''
                mkdir $out
                cp out/aoc2022.jar $out
            '';
          };
        };
      };
      flake = {

      };
    };
}